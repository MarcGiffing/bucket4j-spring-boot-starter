package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.predicate.ServletRequestExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;

import io.github.bucket4j.grid.jcache.JCacheProxyManager;
import lombok.extern.slf4j.Slf4j;

/**
 * Configures {@link Filter}s for Bucket4Js rate limit.
 */
@Configuration
@ConditionalOnClass({ Filter.class, JCacheProxyManager.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.class)
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {ServletRequestExecutePredicateConfiguration.class, Bucket4JAutoConfigurationServletFilterBeans.class, Bucket4jCacheConfiguration.class, SpringBootActuatorConfig.class })
@Slf4j
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration<HttpServletRequest> implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>   {

	private final ConfigurableBeanFactory beanFactory;
	
    private final GenericApplicationContext context;

	private final List<MetricHandler> metricHandlers;
	
	private final Map<String, ExecutePredicate<HttpServletRequest>> executePredicates;
	
	private final Bucket4jConfigurationHolder servletConfigurationHolder;
	
	private final ExpressionParser servletFilterExpressionParser;
	
	public Bucket4JAutoConfigurationServletFilter(
			Bucket4JBootProperties properties,
			ConfigurableBeanFactory beanFactory,
			GenericApplicationContext context,
			SyncCacheResolver cacheResolver,
			List<MetricHandler> metricHandlers,
			List<ExecutePredicate<HttpServletRequest>> executePredicates,
			Bucket4jConfigurationHolder servletConfigurationHolder,
			ExpressionParser servletFilterExpressionParser) {
		super(properties, cacheResolver);
		this.beanFactory = beanFactory;
		this.context = context;
		this.metricHandlers = metricHandlers;
		this.executePredicates = executePredicates
				.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity()));
		this.servletConfigurationHolder =  servletConfigurationHolder;
		this.servletFilterExpressionParser = servletFilterExpressionParser;
	}
	
	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		var filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.SERVLET))
			.map(filter -> properties.isFilterConfigCachingEnabled() ? getOrUpdateConfigurationFromCache(filter) :	filter)
			.forEach(filter -> {
				addDefaultMetricTags(properties, filter);
				filterCount.incrementAndGet();
				var filterConfig = buildFilterConfig(
						filter,
						cacheResolver.resolve(filter.getCacheName()), 
						servletFilterExpressionParser, beanFactory);

				servletConfigurationHolder.addFilterConfiguration(filter);

				//Use either the filter id as bean name or the prefix + counter if no id is configured
				String beanName = filter.getId() != null ? filter.getId() : ("bucket4JServletRequestFilter" + filterCount);
				context.registerBean(beanName, Filter.class, () -> new ServletRequestFilter(filterConfig));

				log.info("create-servlet-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
			});
	}

	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}

	@Override
	protected ExecutePredicate<HttpServletRequest> getExecutePredicateByName(String name) {
		return executePredicates.getOrDefault(name, null);
	}

	@Override
	public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
		//only handle servlet filter updates
		Bucket4JConfiguration newConfig = event.getNewValue();
		if(newConfig.getFilterMethod().equals(FilterMethod.SERVLET)) {
			try {
				ServletRequestFilter filter = context.getBean(event.getKey(), ServletRequestFilter.class);
				FilterConfiguration<HttpServletRequest> newFilterConfig = buildFilterConfig(
						newConfig,
						cacheResolver.resolve(newConfig.getCacheName()),
						servletFilterExpressionParser, beanFactory);
				filter.setFilterConfig(newFilterConfig);
			} catch (Exception exception) {
				log.warn("Failed to update Servlet Filter configuration. {}", exception.getMessage());
			}
		}
	}
}
