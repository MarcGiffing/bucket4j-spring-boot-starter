package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.webflux;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.predicate.WebfluxExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.service.ServiceConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxRateLimitFilter;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxRateLimiterFilterFactory;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.reactive.WebFluxAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.server.WebFilter;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configures Servlet Filters for Bucket4Js rate limit.
 */
@Configuration
@ConditionalOnBucket4jEnabled
@ConditionalOnClass({ WebFilter.class })
@AutoConfigureBefore(value = { WebFluxAutoConfiguration.class })
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = AsyncCacheResolver.class)
@EnableConfigurationProperties({ Bucket4JBootProperties.class})
@Import(value = { ServiceConfiguration.class, WebfluxExecutePredicateConfiguration.class, Bucket4JAutoConfigurationWebfluxFilterBeans.class, SpringBootActuatorConfig.class })
@Slf4j
public class Bucket4JAutoConfigurationWebfluxFilter extends Bucket4JBaseConfiguration<ServerHttpRequest, ServerHttpResponse> {

	private final Bucket4JBootProperties properties;

    private final GenericApplicationContext context;

	private final AsyncCacheResolver cacheResolver;

	private final Bucket4jConfigurationHolder servletConfigurationHolder;

	private final WebfluxRateLimiterFilterFactory webfluxRateLimiterFilterFactory;

	public Bucket4JAutoConfigurationWebfluxFilter(
            Bucket4JBootProperties properties,
            GenericApplicationContext context,
            AsyncCacheResolver cacheResolver,
            List<MetricHandler> metricHandlers,
            List<ExecutePredicate<ServerHttpRequest>> executePredicates,
            Bucket4jConfigurationHolder servletConfigurationHolder,
            RateLimitService rateLimitService,
            @Autowired(required = false) CacheManager<String, Bucket4JConfiguration> configCacheManager,
			WebfluxRateLimiterFilterFactory webfluxRateLimiterFilterFactory) {
		super(rateLimitService, configCacheManager, metricHandlers, executePredicates
				.stream()
				.collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
		this.properties = properties;
		this.context = context;
		this.cacheResolver = cacheResolver;
		this.servletConfigurationHolder = servletConfigurationHolder;
        this.webfluxRateLimiterFilterFactory = webfluxRateLimiterFilterFactory;
    }

	@PostConstruct
	public void initFilters() {
		AtomicInteger filterCount = new AtomicInteger(0);
		properties
			.getFilters()
			.stream()
			.filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.WEBFLUX))
			.map(filter -> properties.isFilterConfigCachingEnabled() ? getOrUpdateConfigurationFromCache(filter) : filter)
			.forEach(filter -> {
				setDefaults(properties, filter);
				filterCount.incrementAndGet();
				var filterConfig = buildFilterConfig(filter, cacheResolver.resolve(filter.getCacheName()));

				servletConfigurationHolder.addFilterConfiguration(filter);

				//Use either the filter id as bean name or the prefix + counter if no id is configured
				String beanName = filter.getId() != null ? filter.getId() : ("bucket4JWebfluxFilter" + filterCount);
				context.registerBean(
						beanName,
						WebfluxRateLimitFilter.class,
						() -> webfluxRateLimiterFilterFactory.create(filterConfig));

				log.info("create-webflux-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
			});
	}


	@Override
	public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
		//only handle webflux filter updates
		Bucket4JConfiguration newConfig = event.getNewValue();
		if (newConfig.getFilterMethod().equals(FilterMethod.WEBFLUX)) {
			try {
				var filter = context.getBean(event.getKey(), WebfluxRateLimitFilter.class);
				var newFilterConfig = buildFilterConfig(newConfig, cacheResolver.resolve(newConfig.getCacheName()));
				filter.setFilterConfig(newFilterConfig);
			} catch (Exception exception) {
				log.warn("Failed to update Webflux Filter configuration. {}", exception.getMessage());
			}
		}
	}
}
