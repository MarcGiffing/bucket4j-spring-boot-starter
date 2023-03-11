package com.giffing.bucket4j.spring.boot.starter.config.servlet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.MethodExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.PathExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.ServletRequestExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate.ServletRequestExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateBeanNotFoundException;
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
@Import(value = {ServletRequestExecutePredicateConfiguration.class, Bucket4JAutoConfigurationServletFilterBeans.class, Bucket4jCacheConfiguration.class, SpringBootActuatorConfig.class })
@Slf4j
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration<HttpServletRequest> implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>   {

	private final Bucket4JBootProperties properties;
	
	private final ConfigurableBeanFactory beanFactory;
	
    private final GenericApplicationContext context;
	
	private final SyncCacheResolver cacheResolver;

	private final List<MetricHandler> metricHandlers;
	
	private final Map<String, ServletRequestExecutePredicate> executePredicates;
	
	private Bucket4jConfigurationHolder servletConfigurationHolder;
	
	private ExpressionParser servletFilterExpressionParser;
	
	public Bucket4JAutoConfigurationServletFilter(
			Bucket4JBootProperties properties,
			ConfigurableBeanFactory beanFactory,
			GenericApplicationContext context,
			SyncCacheResolver cacheResolver,
			List<MetricHandler> metricHandlers,
			List<ServletRequestExecutePredicate> executePredicates,
			Bucket4jConfigurationHolder servletConfigurationHolder,
			ExpressionParser servletFilterExpressionParser) {
		this.properties = properties;
		this.beanFactory = beanFactory;
		this.context = context;
		this.cacheResolver = cacheResolver;
		this.metricHandlers = metricHandlers;
		this.executePredicates = executePredicates
				.stream()
				.collect(Collectors.toMap(ServletRequestExecutePredicate::name, Function.identity()));
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
						filterCount.incrementAndGet();
						FilterConfiguration<HttpServletRequest> filterConfig = buildFilterConfig(
								filter,
								cacheResolver.resolve(filter.getCacheName()), 
								servletFilterExpressionParser, beanFactory);
	
						servletConfigurationHolder.addFilterConfiguration(filter);
	
						context.registerBean("bucket4JServletRequestFilter" + filterCount, Filter.class, () -> new ServletRequestFilter(filterConfig));
						log.info("create-servlet-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
					});
			}

	@Override
	public List<MetricHandler> getMetricHandlers() {
		return this.metricHandlers;
	}

	@Override
	protected boolean predicates(RateLimit rl, HttpServletRequest servletRequest) {
		var predicate = rl.getExecutePredicates()
        		.stream()
        		.map(p -> createPredicate(p))
        		.reduce( (p1, p2) -> p1.and(p2))
        		.get();
        	return predicate.test(servletRequest);
	}
	
	private Predicate<HttpServletRequest> createPredicate(ExecutePredicateDefinition pd) {
		var predicate = executePredicates.get(pd.getName());
		log.debug("create-predicate;name:{};value:{}", pd.getName(), pd.getConfigValue());
		return predicate.newInstance().setValue(pd.getConfigValue());
	}

	@Override
	protected void validate(Bucket4JConfiguration config) {
		var allExecutePredicateNames = config
			.getRateLimits()
			.stream()
			.map(r -> r.getExecutePredicates())
			.flatMap(List::stream)
			.map(x -> x.getName())
			.distinct().collect(Collectors.toSet());
		allExecutePredicateNames.forEach(predicateName -> {
			if(!executePredicates.containsKey(predicateName)) {
				throw new ExecutePredicateBeanNotFoundException(predicateName);
			}
		});
	}

}
