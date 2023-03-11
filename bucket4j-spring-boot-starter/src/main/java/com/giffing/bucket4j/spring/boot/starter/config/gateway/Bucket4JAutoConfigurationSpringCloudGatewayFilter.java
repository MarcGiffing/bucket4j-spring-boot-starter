package com.giffing.bucket4j.spring.boot.starter.config.gateway;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.SpringCloudGatewayRateLimitFilter;
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
@Import(value = { SpringBootActuatorConfig.class, Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans.class })
public class Bucket4JAutoConfigurationSpringCloudGatewayFilter extends Bucket4JBaseConfiguration<ServerHttpRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationSpringCloudGatewayFilter.class);

	private final Bucket4JBootProperties properties;

	private final ConfigurableBeanFactory beanFactory;

	private final GenericApplicationContext context;

	private final AsyncCacheResolver cacheResolver;

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
		this.properties = properties;
		this.beanFactory = beanFactory;
		this.context = context;
		this.cacheResolver = cacheResolver;
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
			.map(filter -> {
				addDefaultMetricTags(properties, filter);
				filterCount.incrementAndGet();
				FilterConfiguration<ServerHttpRequest> filterConfig = buildFilterConfig(
						filter, 
						cacheResolver.resolve(filter.getCacheName()), 
						gatewayFilterExpressionParser, 
						beanFactory);
				
				gatewayConfigurationHolder.addFilterConfiguration(filter);
				
				SpringCloudGatewayRateLimitFilter webFilter = new SpringCloudGatewayRateLimitFilter(filterConfig);
		        
		        log.info("create-gateway-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
		        return webFilter;
			}).forEach(webFilter ->
				context.registerBean("bucket4JGatewayFilter" + filterCount, GlobalFilter.class, () -> webFilter)
			);
		
	}

	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}


	@Override
	protected boolean predicates(RateLimit rl, ServerHttpRequest servletRequest) {
		return false;
	}


	@Override
	protected void validate(Bucket4JConfiguration config) {
	}


	

}
