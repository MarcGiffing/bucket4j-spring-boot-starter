package com.giffing.bucket4j.spring.boot.starter.servlet.config;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.service.ServiceConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.service.RateLimitService;
import com.giffing.bucket4j.spring.boot.starter.servlet.ServletRateLimitFilter;
import com.giffing.bucket4j.spring.boot.starter.servlet.ServletRateLimiterFilterFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configures {@link Filter}s for Bucket4Js rate limit.
 */
@AutoConfiguration
@ConditionalOnBucket4jEnabled
@ConditionalOnClass({Filter.class})
@EnableConfigurationProperties({Bucket4JBootProperties.class})
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {
        ServiceConfiguration.class,
        ServletRequestExecutePredicateConfiguration.class,
        Bucket4JAutoConfigurationServletFilterBeans.class,
        SpringBootActuatorConfig.class
})
@Slf4j
public class Bucket4JAutoConfigurationServletFilter extends com.giffing.bucket4j.spring.boot.starter.core.Bucket4JBaseConfiguration<HttpServletRequest, HttpServletResponse>
        implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final Bucket4JBootProperties properties;

    private final GenericApplicationContext context;

    private final SyncCacheResolver cacheResolver;

    private final Bucket4jConfigurationHolder servletConfigurationHolder;

    private final ServletRateLimiterFilterFactory servletRateLimiterFilterFactory;

    public Bucket4JAutoConfigurationServletFilter(
            Bucket4JBootProperties properties,
            GenericApplicationContext context,
            SyncCacheResolver cacheResolver,
            List<MetricHandler> metricHandlers,
            List<ExecutePredicate<HttpServletRequest>> executePredicates,
            Bucket4jConfigurationHolder servletConfigurationHolder,
            RateLimitService rateLimitService,
            @Autowired(required = false) CacheManager<String, Bucket4JConfiguration> configCacheManager,
            ServletRateLimiterFilterFactory servletRateLimiterFilterFactory) {
        super(rateLimitService, configCacheManager, metricHandlers, executePredicates
                .stream()
                .collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
        this.properties = properties;
        this.context = context;
        this.cacheResolver = cacheResolver;
        this.servletConfigurationHolder = servletConfigurationHolder;
        this.servletRateLimiterFilterFactory = servletRateLimiterFilterFactory;
    }

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        var filterCount = new AtomicInteger(0);
        properties
                .getFilters()
                .stream()
                .filter(filter -> StringUtils.hasText(filter.getUrl()) && filter.getFilterMethod().equals(FilterMethod.SERVLET))
                .map(filter -> properties.isFilterConfigCachingEnabled() ? getOrUpdateConfigurationFromCache(filter) : filter)
                .forEach(filter -> {
                    setDefaults(properties, filter);
                    filterCount.incrementAndGet();
                    var filterConfig = buildFilterConfig(filter, cacheResolver.resolve(filter.getCacheName()));

                    servletConfigurationHolder.addFilterConfiguration(filter);

                    //Use either the filter id as bean name or the prefix + counter if no id is configured
                    var beanName = filter.getId() != null ? filter.getId() : ("bucket4JServletRequestFilter" + filterCount);
                    context.registerBean(
                            beanName,
                            ServletRateLimitFilter.class,
                            () -> servletRateLimiterFilterFactory.create(filterConfig));

                    log.info("create-servlet-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
                });
    }

    @Override
    public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
        //only handle servlet filter updates
        Bucket4JConfiguration newConfig = event.getNewValue();
        if (newConfig.getFilterMethod().equals(FilterMethod.SERVLET)) {
            try {
                var filter = context.getBean(event.getKey(), ServletRateLimitFilter.class);
                var newFilterConfig = buildFilterConfig(newConfig, cacheResolver.resolve(newConfig.getCacheName()));
                filter.setFilterConfig(newFilterConfig);
            } catch (Exception exception) {
                log.warn("Failed to update Servlet Filter configuration. {}", exception.getMessage());
            }
        }
    }


}
