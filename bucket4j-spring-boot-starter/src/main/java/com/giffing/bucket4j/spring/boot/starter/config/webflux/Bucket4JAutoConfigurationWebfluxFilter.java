package com.giffing.bucket4j.spring.boot.starter.config.webflux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.WebFilter;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.ServletRequestExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.webflux.predicate.WebfluxExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxWebFilter;

import jakarta.annotation.PostConstruct;

/**
 * Configures Servlet Filters for Bucket4Js rate limit.
 * 
 */
@Configuration
@ConditionalOnClass({ WebFilter.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = { "enabled" }, matchIfMissing = true)
@AutoConfigureBefore(value = {WebFluxAutoConfiguration.class})
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = AsyncCacheResolver.class)
@EnableConfigurationProperties({ Bucket4JBootProperties.class})
@Import(value = { WebfluxExecutePredicateConfiguration.class, Bucket4JAutoConfigurationWebfluxFilterBeans.class, SpringBootActuatorConfig.class })
public class Bucket4JAutoConfigurationWebfluxFilter extends Bucket4JBaseConfiguration<ServerHttpRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationWebfluxFilter.class);

	private final Bucket4JBootProperties properties;
	
	private final ConfigurableBeanFactory beanFactory;
	
    private final GenericApplicationContext context;
	
	private final AsyncCacheResolver cacheResolver;

	private final List<MetricHandler> metricHandlers;
	
	private final Map<String, ExecutePredicate<ServerHttpRequest>> executePredicates;
	
	private final Bucket4jConfigurationHolder servletConfigurationHolder;

	private final ExpressionParser webfluxFilterExpressionParser;
	
	public Bucket4JAutoConfigurationWebfluxFilter(
			Bucket4JBootProperties properties,
			ConfigurableBeanFactory beanFactory,
			GenericApplicationContext context,
			AsyncCacheResolver cacheResolver,
			List<MetricHandler> metricHandlers,
			List<ExecutePredicate<ServerHttpRequest>> executePredicates,
			Bucket4jConfigurationHolder servletConfigurationHolder,
			ExpressionParser webfluxFilterExpressionParser) {
		this.properties = properties;
		this.beanFactory = beanFactory;
		this.context = context;
		this.cacheResolver = cacheResolver;
		this.metricHandlers = metricHandlers;
		this.executePredicates = executePredicates
				.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity()));
		this.servletConfigurationHolder = servletConfigurationHolder;
		this.webfluxFilterExpressionParser = webfluxFilterExpressionParser;
	}
	
	@PostConstruct
	public void initFilters() {
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.WEBFLUX))
			.map(filter -> {
				addDefaultMetricTags(properties, filter);
				filterCount.incrementAndGet();
				FilterConfiguration<ServerHttpRequest> filterConfig = buildFilterConfig(filter, cacheResolver.resolve(
						filter.getCacheName()), 
						webfluxFilterExpressionParser, 
						beanFactory);
				
				servletConfigurationHolder.addFilterConfiguration(filter);
				
				WebFilter webFilter = new WebfluxWebFilter(filterConfig);
		        
		        log.info("create-webflux-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
		        return webFilter;
			}).forEach(webFilter -> 
				context.registerBean("bucket4JWebfluxFilter" + filterCount, WebFilter.class, () -> webFilter)
			);
		
	}
	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}

	@Override
	protected ExecutePredicate<ServerHttpRequest> getExecutePredicateByName(String name) {
		return executePredicates.getOrDefault(name, null);
	}

}
