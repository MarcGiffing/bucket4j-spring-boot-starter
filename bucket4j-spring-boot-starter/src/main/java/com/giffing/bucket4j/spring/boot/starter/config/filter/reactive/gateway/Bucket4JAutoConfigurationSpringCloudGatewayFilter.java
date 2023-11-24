package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.predicate.WebfluxExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.SpringCloudGatewayRateLimitFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Configures Servlet Filters for Bucket4Js rate limit.
 *
 */
@Configuration
@ConditionalOnClass({ GlobalFilter.class })
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = { "enabled" }, matchIfMissing = true)
@AutoConfigureBefore(GatewayAutoConfiguration.class)
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = AsyncCacheResolver.class)
@Import(value = { WebfluxExecutePredicateConfiguration.class, SpringBootActuatorConfig.class, Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans.class })
public class Bucket4JAutoConfigurationSpringCloudGatewayFilter extends Bucket4JBaseConfiguration<ServerHttpRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationSpringCloudGatewayFilter.class);

	private final ConfigurableBeanFactory beanFactory;

	private final GenericApplicationContext context;

	private final List<MetricHandler> metricHandlers;

	private Bucket4jConfigurationHolder gatewayConfigurationHolder;

	private ExpressionParser gatewayFilterExpressionParser;

	public Bucket4JAutoConfigurationSpringCloudGatewayFilter(
			Bucket4JBootProperties properties,
			ConfigurableBeanFactory beanFactory,
			GenericApplicationContext context,
			AsyncCacheResolver cacheResolver,
			List<MetricHandler> metricHandlers,
			Bucket4jConfigurationHolder gatewayConfigurationHolder,
			ExpressionParser gatewayFilterExpressionParser) {
		super(properties, cacheResolver);
		this.beanFactory = beanFactory;
		this.context = context;
		this.metricHandlers = metricHandlers;
		this.gatewayConfigurationHolder = gatewayConfigurationHolder;
		this.gatewayFilterExpressionParser = gatewayFilterExpressionParser;

		initFilters();
	}


	public void initFilters() {
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.GATEWAY))
			.map(filter -> properties.isFilterConfigCachingEnabled() ? getOrUpdateConfigurationFromCache(filter) :	filter)
			.forEach(filter -> {
				addDefaultMetricTags(properties, filter);
				filterCount.incrementAndGet();
				FilterConfiguration<ServerHttpRequest> filterConfig = buildFilterConfig(
						filter,
						cacheResolver.resolve(filter.getCacheName()),
						gatewayFilterExpressionParser,
						beanFactory);

				gatewayConfigurationHolder.addFilterConfiguration(filter);

				//Use either the filter id as bean name or the prefix + counter if no id is configured
				String beanName = filter.getId() != null ? filter.getId() : ("bucket4JGatewayFilter" + filterCount);
				context.registerBean(beanName, GlobalFilter.class, () -> new SpringCloudGatewayRateLimitFilter(filterConfig));

				log.info("create-gateway-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
			});

	}

	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}


	@Override
	protected ExecutePredicate<ServerHttpRequest> getExecutePredicateByName(String name) {
		throw new UnsupportedOperationException("Execution predicates not supported");
	}

	@Override
	public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
		//only handle servlet filter updates
		Bucket4JConfiguration newConfig = event.getNewValue();
		if(newConfig.getFilterMethod().equals(FilterMethod.GATEWAY)) {
			try {
				SpringCloudGatewayRateLimitFilter filter = context.getBean(event.getKey(), SpringCloudGatewayRateLimitFilter.class);
				FilterConfiguration<ServerHttpRequest> newFilterConfig = buildFilterConfig(
						newConfig,
						cacheResolver.resolve(newConfig.getCacheName()),
						gatewayFilterExpressionParser,
						beanFactory);
				filter.setFilterConfig(newFilterConfig);
			} catch (Exception exception) {
				log.warn("Failed to update Gateway Filter configuration. {}", exception.getMessage());
			}
		}
	}
}
