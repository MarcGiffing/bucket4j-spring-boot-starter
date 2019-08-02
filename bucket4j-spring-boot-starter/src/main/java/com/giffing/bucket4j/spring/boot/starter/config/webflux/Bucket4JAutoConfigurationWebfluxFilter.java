package com.giffing.bucket4j.spring.boot.starter.config.webflux;

import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.WebFilter;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.webflux.WebfluxWebFilter;

/**
 * Configures Servlet Filters for Bucket4Js rate limit.
 * 
 */
@Configuration
@ConditionalOnClass({ WebFilter.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = { "enabled" }, matchIfMissing = true)
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = AsyncCacheResolver.class)
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@Import(value = { SpringBootActuatorConfig.class })
public class Bucket4JAutoConfigurationWebfluxFilter extends Bucket4JBaseConfiguration<ServerHttpRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationWebfluxFilter.class);

	@Autowired
	private Bucket4JBootProperties properties;

	@Autowired
	private ConfigurableBeanFactory beanFactory;

	@Autowired
	private GenericApplicationContext context;

	@Autowired
	private AsyncCacheResolver cacheResolver;

	@Bean
	@Qualifier("WEBFLUX")
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}

	@Bean
	public ExpressionParser webFilterExpressionParser() {
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE,
				this.getClass().getClassLoader());
		ExpressionParser parser = new SpelExpressionParser(config);

		return parser;
	}
	
	@PostConstruct
	public void initFilters() {
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> !StringUtils.isEmpty(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.WEBFLUX))
			.map(filter -> {
				filterCount.incrementAndGet();
				FilterConfiguration<ServerHttpRequest> filterConfig = buildFilterConfig(filter, cacheResolver.resolve(
						filter.getCacheName()), 
						webFilterExpressionParser(), 
						beanFactory);
				
				servletConfigurationHolder().addFilterConfiguration(filter);
				
				WebFilter webFilter = new WebfluxWebFilter(filterConfig);
		        
		        log.info("create-webflux-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
		        return webFilter;
			}).forEach(webFilter -> {
				context.registerBean("bucket4JWebfluxFilter" + filterCount, WebFilter.class, () -> webFilter);
			});
		
	}


	

}
