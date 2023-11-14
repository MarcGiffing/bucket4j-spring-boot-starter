package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.webflux;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.SpringCloudGatewayRateLimitFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
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

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.predicate.WebfluxExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.SpringBootActuatorConfig;
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

	private final ConfigurableBeanFactory beanFactory;
	
    private final GenericApplicationContext context;

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
		super(properties, cacheResolver);
		this.beanFactory = beanFactory;
		this.context = context;
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
			.map(filter -> properties.isFilterConfigCachingEnabled() ? getOrUpdateConfigurationFromCache(filter) :	filter)
			.forEach(filter -> {
				addDefaultMetricTags(properties, filter);
				filterCount.incrementAndGet();
				FilterConfiguration<ServerHttpRequest> filterConfig = buildFilterConfig(filter, cacheResolver.resolve(
						filter.getCacheName()), 
						webfluxFilterExpressionParser, 
						beanFactory);
				
				servletConfigurationHolder.addFilterConfiguration(filter);

				//Use either the filter id as bean name or the prefix + counter if no id is configured
				String beanName = filter.getId() != null ? filter.getId() : ("bucket4JWebfluxFilter" + filterCount);
				context.registerBean(beanName, WebFilter.class, () -> new WebfluxWebFilter(filterConfig));

		        log.info("create-webflux-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
			});
	}
	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}

	@Override
	protected ExecutePredicate<ServerHttpRequest> getExecutePredicateByName(String name) {
		return executePredicates.getOrDefault(name, null);
	}

	@Override
	public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
		//only handle servlet filter updates
		Bucket4JConfiguration newConfig = event.getNewValue();
		if(newConfig.getFilterMethod().equals(FilterMethod.WEBFLUX)) {
			try {
				WebfluxWebFilter filter = context.getBean(event.getKey(), WebfluxWebFilter.class);
				FilterConfiguration<ServerHttpRequest> newFilterConfig = buildFilterConfig(
						newConfig,
						cacheResolver.resolve(newConfig.getCacheName()),
						webfluxFilterExpressionParser,
						beanFactory);
				filter.setFilterConfig(newFilterConfig);
			} catch (BeansException exception) {
				log.warn("Failed to update Webflux Filter configuration. {}", exception.getMessage());
			}
		}
	}
}
