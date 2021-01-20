package com.giffing.bucket4j.spring.boot.starter.config.zuul;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.zuul.ZuulRateLimitFilter;
import com.netflix.zuul.FilterFactory;
import com.netflix.zuul.ZuulFilter;

import io.github.bucket4j.grid.jcache.JCache;

/**
 * Configures {@link ZuulFilter}s for Bucket4Js rate limit.
 * 
 */
@Configuration
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = { "enabled" }, matchIfMissing = true)
@ConditionalOnClass({ FilterFactory.class, JCache.class })
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = SyncCacheResolver.class)
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@Import(value = {Bucket4jCacheConfiguration.class, SpringBootActuatorConfig.class })
public class Bucket4JAutoConfigurationZuul extends Bucket4JBaseConfiguration<HttpServletRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationZuul.class);

	private final Bucket4JBootProperties properties;
	
	private final ConfigurableBeanFactory beanFactory;
	
    private final GenericApplicationContext context;
	
	private final SyncCacheResolver cacheResolver;

	private final List<MetricHandler> metricHandlers;
	
	public Bucket4JAutoConfigurationZuul(
			Bucket4JBootProperties properties,
			ConfigurableBeanFactory beanFactory,
			GenericApplicationContext context,
			SyncCacheResolver cacheResolver,
			List<MetricHandler> metricHandlers) {
		this.properties = properties;
		this.beanFactory = beanFactory;
		this.context = context;
		this.cacheResolver = cacheResolver;
		this.metricHandlers = metricHandlers;
	}
	@Bean
	@Qualifier("ZUUL")
	public Bucket4jConfigurationHolder zuulConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}
	
	@Bean
	public ExpressionParser zuulExpressionParser() {
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
			.filter(filter -> !StringUtils.isEmpty(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.ZUUL))
			.map(filter -> {
				filterCount.incrementAndGet();
				
				FilterConfiguration<HttpServletRequest> filterConfig = buildFilterConfig(filter, 
						cacheResolver.resolve(filter.getCacheName()),
						zuulExpressionParser(), beanFactory);
				
				zuulConfigurationHolder().addFilterConfiguration(filter);
		        
		        log.info("create-zuul-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
		        return new ZuulRateLimitFilter(filterConfig);
			}).forEach(filter -> {
				context.registerBean("bucket4JZuulFilter" + filterCount, ZuulRateLimitFilter.class, () -> filter);
			});
	}
	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}

}
