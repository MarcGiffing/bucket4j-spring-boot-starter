package com.giffing.bucket4j.spring.boot.starter.config.servlet;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBoot1ActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBoot2ActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.servlet.ServletRequestFilter;

import io.github.bucket4j.grid.jcache.JCache;

/**
 * Configures {@link Filter}s for Bucket4Js rate limit.
 * 
 */
@Configuration
@ConditionalOnClass({ Filter.class, JCache.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {Bucket4jCacheConfiguration.class, SpringBoot1ActuatorConfig.class, SpringBoot2ActuatorConfig.class })
@Order(Ordered.HIGHEST_PRECEDENCE)
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration<HttpServletRequest> implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>   {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationServletFilter.class);
	
	@Autowired
	private Bucket4JBootProperties properties;
	
	@Autowired
	private ConfigurableBeanFactory beanFactory;
	
	@Autowired
    private GenericApplicationContext context;
	
	@Autowired
	private SyncCacheResolver cacheResolver;

	@Bean
	@Qualifier("SERVLET")
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}
	
	@Bean
	public ExpressionParser servletFilterExpressionParser() {
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader());
		ExpressionParser parser = new SpelExpressionParser(config);
		
		return parser;
	}

	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
				AtomicInteger filterCount = new AtomicInteger(0);
				properties
					.getFilters()
					.stream()
					.filter(filter -> !StringUtils.isEmpty(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.SERVLET))
					.forEach(filter -> {
						filterCount.incrementAndGet();
						FilterConfiguration<HttpServletRequest> filterConfig = buildFilterConfig(filter,
								cacheResolver.resolve(filter.getCacheName()), servletFilterExpressionParser(), beanFactory);
	
						servletConfigurationHolder().addFilterConfiguration(filter);
	
						context.registerBean("bucket4JServletRequestFilter" + filterCount, Filter.class, () -> new ServletRequestFilter(filterConfig));
						log.info("create-servlet-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
					});
			}

}
