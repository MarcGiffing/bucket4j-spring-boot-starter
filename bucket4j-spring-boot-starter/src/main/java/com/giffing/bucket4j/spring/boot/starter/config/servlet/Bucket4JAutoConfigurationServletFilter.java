package com.giffing.bucket4j.spring.boot.starter.config.servlet;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.Bucket4jMetricConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBoot1ActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBoot2ActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.servlet.ServletRequestFilter;

import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;

/**
 * Configures up to 10 Servlet {@link Filter}s for Bucket4Js rate limit.
 * 
 * Technical problem: The dynamic creation of the {@link FilterRegistrationBean}s failed cause when
 * registering them manually in the application context the Beans arn't detected as {@link Filter}s
 * and therefore not configured correctly. The current workaround is the define 10 different methods
 * which creates individual {@link FilterRegistrationBean}s conditional on properties.
 * 
 */
@Configuration
@ConditionalOnClass({ Filter.class, JCache.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {Bucket4jCacheConfiguration.class, Bucket4jMetricConfiguration.class, SpringBoot1ActuatorConfig.class, SpringBoot2ActuatorConfig.class })
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration<HttpServletRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationServletFilter.class);
	
	@Autowired
	private Bucket4JBootProperties properties;
	
	@Autowired
	private ConfigurableBeanFactory beanFactory;
	
	@Autowired
    private GenericWebApplicationContext context;
	
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
	
	@PostConstruct
	public void initFilters() {
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> !StringUtils.isEmpty(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.SERVLET))
			.map(filter -> {
				filterCount.incrementAndGet();
				FilterConfiguration<HttpServletRequest> filterConfig = buildFilterConfig(filter, cacheResolver.resolve(
						filter.getCacheName()), 
						servletFilterExpressionParser(), 
						beanFactory);
				
				servletConfigurationHolder().addFilterConfiguration(filter);
				
				FilterRegistrationBean<ServletRequestFilter> registration = new FilterRegistrationBean<>();
				registration.setName("bucket4JRequestFilter" + filterCount);
		        registration.setFilter(new ServletRequestFilter(filterConfig));
		        registration.addUrlPatterns(filter.getUrl());
		        registration.setOrder(filter.getFilterOrder());
		        
		        log.info("create-servlet-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
		        return registration;
			}).forEach(filterRegistrationBean -> {
				context.registerBean("bucket4JFilter" + filterCount, FilterRegistrationBean.class, () -> filterRegistrationBean);
			});
	}

}
