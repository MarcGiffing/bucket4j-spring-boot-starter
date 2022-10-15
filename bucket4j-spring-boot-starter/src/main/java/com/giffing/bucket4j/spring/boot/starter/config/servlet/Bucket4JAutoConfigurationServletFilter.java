package com.giffing.bucket4j.spring.boot.starter.config.servlet;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.refresh.ApplicationRefreshConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.refresh.Bucket4jRefreshEvent;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;

import io.github.bucket4j.grid.jcache.JCacheProxyManager;

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
@Import(value = {Bucket4JAutoConfigurationServletFilterBeans.class, Bucket4jCacheConfiguration.class, SpringBootActuatorConfig.class, ApplicationRefreshConfiguration.class })
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration<HttpServletRequest> implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>   {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationServletFilter.class);
	
	private final Bucket4JBootProperties properties;
	
	private final ConfigurableBeanFactory beanFactory;
	
    private final GenericApplicationContext context;
	
	private final SyncCacheResolver cacheResolver;

	private final List<MetricHandler> metricHandlers;

	private Bucket4jConfigurationHolder servletConfigurationHolder;
	
	private ExpressionParser servletFilterExpressionParser;
	
	public Bucket4JAutoConfigurationServletFilter(
			Bucket4JBootProperties properties,
			ConfigurableBeanFactory beanFactory,
			GenericApplicationContext context,
			SyncCacheResolver cacheResolver,
			List<MetricHandler> metricHandlers,
			Bucket4jConfigurationHolder servletConfigurationHolder,
			ExpressionParser servletFilterExpressionParser) {
		this.properties = properties;
		this.beanFactory = beanFactory;
		this.context = context;
		this.cacheResolver = cacheResolver;
		this.metricHandlers = metricHandlers;
		this.servletConfigurationHolder =  servletConfigurationHolder;
		this.servletFilterExpressionParser = servletFilterExpressionParser;
	}
	
	@Override
	public void customize(ConfigurableServletWebServerFactory factory) {
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.SERVLET))
			.forEach(filter -> {
				addDefaultMetricTags(properties, filter);
				Integer filterIndex = filterCount.incrementAndGet();
				FilterConfiguration<HttpServletRequest> filterConfig = buildFilterConfig(
						filter,
						cacheResolver.resolve(filter.getCacheName()), 
						servletFilterExpressionParser, beanFactory);

				servletConfigurationHolder.addFilterConfiguration(filter);
				context.registerBean("bucket4JServletRequestFilter" + filterIndex, Filter.class, () -> new ServletRequestFilter(filterConfig, filterIndex));
				log.info("create-servlet-filter;{};{};{}", filterIndex, filter.getCacheName(), filter.getUrl());
			});
	}
	
	
	/**
	 * Handles the {@link Bucket4jRefreshEvent} and updates the Bucket4j configuration of all
	 * {@link ServletRequestFilter}s.
	 * 
	 * @param event
	 */
	@EventListener(Bucket4jRefreshEvent.class)
	public void handleBucket4jRefreshEvent(Bucket4jRefreshEvent event) {
		Map<String, ServletRequestFilter> filters = context.getBeansOfType(ServletRequestFilter.class);
		
		// FILTER out FilterMethod.SERVLET to reset it with the new configuration
		servletConfigurationHolder.setFilterConfiguration(
				servletConfigurationHolder
				.getFilterConfiguration()
				.stream()
				.filter(f -> !f.getFilterMethod().equals(FilterMethod.SERVLET))
				.collect(Collectors.toList())
				);
		
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
		.getFilters()
		.stream()
		.filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.SERVLET))
		.forEach(filter -> {
			addDefaultMetricTags(properties, filter);
			Integer filterIndex = filterCount.incrementAndGet();
			java.util.Optional<ServletRequestFilter> servletRequestFilter = filters
				.entrySet()
				.stream()
				.filter( entry -> entry.getValue().getFilterIndex().equals(filterIndex))
				.map(Entry::getValue)
				.findFirst();
			
			if(servletRequestFilter.isPresent()) {
				
				FilterConfiguration<HttpServletRequest> filterConfig = buildFilterConfig(
						filter,
						cacheResolver.resolve(filter.getCacheName()), 
						servletFilterExpressionParser, beanFactory);
				servletConfigurationHolder.addFilterConfiguration(filter);
				servletRequestFilter.get().updateFilterConfig(filterConfig);
				log.info("update-servlet-filter;{};{};{}", filterIndex, filter.getCacheName(), filter.getUrl());
			}
			
		});
	}
	
	

	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}

}
