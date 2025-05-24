package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.predicate.ServletRequestExecutePredicateConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.service.ServiceConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRequestFilter;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configures {@link Filter}s for Bucket4Js rate limit.
 */
@Configuration
@ConditionalOnBucket4jEnabled
@ConditionalOnClass({Filter.class})
@EnableConfigurationProperties({Bucket4JBootProperties.class})
@AutoConfigureBefore(ServletWebServerFactoryAutoConfiguration.class)
@AutoConfigureAfter(value = {CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class})
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {ServiceConfiguration.class, ServletRequestExecutePredicateConfiguration.class, Bucket4JAutoConfigurationServletFilterBeans.class, Bucket4jCacheConfiguration.class, SpringBootActuatorConfig.class})
@Slf4j
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration<HttpServletRequest, HttpServletResponse>
        implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final Bucket4JBootProperties properties;

    private final GenericApplicationContext context;

    private final SyncCacheResolver cacheResolver;

    private final Bucket4jConfigurationHolder servletConfigurationHolder;

    public Bucket4JAutoConfigurationServletFilter(
            Bucket4JBootProperties properties,
            GenericApplicationContext context,
            SyncCacheResolver cacheResolver,
            List<MetricHandler> metricHandlers,
            List<ExecutePredicate<HttpServletRequest>> executePredicates,
            Bucket4jConfigurationHolder servletConfigurationHolder,
            RateLimitService rateLimitService,
            @Autowired(required = false) CacheManager<String, Bucket4JConfiguration> configCacheManager) {
        super(rateLimitService, configCacheManager, metricHandlers, executePredicates
                .stream()
                .collect(Collectors.toMap(ExecutePredicate::name, Function.identity())));
        this.properties = properties;
        this.context = context;
        this.cacheResolver = cacheResolver;
        this.servletConfigurationHolder = servletConfigurationHolder;
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
                    context.registerBean(beanName, Filter.class, () -> new ServletRequestFilter(filterConfig));

                    log.info("create-servlet-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrl());
                });
    }

    @Override
    public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
        //only handle servlet filter updates
        Bucket4JConfiguration newConfig = event.getNewValue();
        if (newConfig.getFilterMethod().equals(FilterMethod.SERVLET)) {
            try {
                var filter = context.getBean(event.getKey(), ServletRequestFilter.class);
                var newFilterConfig = buildFilterConfig(newConfig, cacheResolver.resolve(newConfig.getCacheName()));
                filter.setFilterConfig(newFilterConfig);
            } catch (Exception exception) {
                log.warn("Failed to update Servlet Filter configuration. {}", exception.getMessage());
            }
        }
    }


}
