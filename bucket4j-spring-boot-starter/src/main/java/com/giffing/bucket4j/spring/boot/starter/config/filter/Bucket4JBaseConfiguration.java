package com.giffing.bucket4j.spring.boot.starter.config.filter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway.Bucket4JAutoConfigurationSpringCloudGatewayFilter;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.context.UrlMapper;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import com.giffing.bucket4j.spring.boot.starter.context.*;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Holds helper Methods which are reused by the
 * {@link Bucket4JAutoConfigurationServletFilter}
 * {@link Bucket4JAutoConfigurationSpringCloudGatewayFilter}
 * {@link Bucket4JAutoConfigurationWebfluxFilter}
 * configuration classes
 */
@Slf4j
public abstract class Bucket4JBaseConfiguration<R, P> implements CacheUpdateListener<String, Bucket4JConfiguration> {

    private final RateLimitService rateLimitService;

    private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

    private final List<MetricHandler> metricHandlers;

    private final Map<String, ExecutePredicate<R>> executePredicates;

    private final UrlMapper urlMapper;

    protected Bucket4JBaseConfiguration(
            RateLimitService rateLimitService,
            CacheManager<String, Bucket4JConfiguration> configCacheManager,
            List<MetricHandler> metricHandlers,
            Map<String, ExecutePredicate<R>> executePredicates,
            @Autowired(required = false) UrlMapper urlMapper) {
        this.rateLimitService = rateLimitService;
        this.configCacheManager = configCacheManager;
        this.metricHandlers = metricHandlers;
        this.executePredicates = executePredicates;
        this.urlMapper = urlMapper;
    }

    public FilterConfiguration<R, P> buildFilterConfig(
            Bucket4JConfiguration config,
            ProxyManagerWrapper proxyWrapper) {

        var rateLimitConfig = RateLimitService.RateLimitConfig.<R>builder()
                .rateLimits(config.getRateLimits())
                .metricHandlers(metricHandlers)
                .executePredicates(executePredicates)
                .cacheName(config.getCacheName())
                .configVersion(config.getBucket4JVersionNumber())
                .keyFunction((rl, sr) -> {
                    KeyFilter<R> keyFilter = rateLimitService.getKeyFilter(config.getUrl(), rl);
                    return keyFilter.key(sr);
                })
                .metrics(config.getMetrics())
                .proxyWrapper(proxyWrapper)
                .build();

        var rateLimitConfigResult = rateLimitService.<R,P>configureRateLimit(rateLimitConfig);

        FilterConfiguration<R, P> filterConfig = mapFilterConfiguration(config);
        rateLimitConfigResult.getRateLimitChecks().forEach(filterConfig::addRateLimitCheck);
        rateLimitConfigResult.getPostRateLimitChecks().forEach(filterConfig::addPostRateLimitCheck);
        
        return filterConfig;
    }



    private FilterConfiguration<R, P> mapFilterConfiguration(Bucket4JConfiguration config) {
        FilterConfiguration<R, P> filterConfig = new FilterConfiguration<>();
        filterConfig.setUrlPattern(config.getUrl());
        filterConfig.setUrlMatcher(
                    urlMapper.getMatcher(
                            config.getUrl()));
        filterConfig.setOrder(config.getFilterOrder());
        filterConfig.setStrategy(config.getStrategy());
        filterConfig.setHttpContentType(config.getHttpContentType());
        filterConfig.setHttpResponseBody(config.getHttpResponseBody());
        filterConfig.setHttpStatusCode(config.getHttpStatusCode());
        filterConfig.setHideHttpResponseHeaders(config.getHideHttpResponseHeaders());
        filterConfig.setHttpResponseHeaders(config.getHttpResponseHeaders());
        filterConfig.setMetrics(config.getMetrics());
        return filterConfig;
    }



    /**
     * Try to load a filter configuration from the cache with the same id as the provided filter.
     * <p>
     * If caching is disabled or no matching filter is found, the provided filter will be added to the cache and returned to the caller.
     * If the provided filter has a higher version than the cached filter, the cache will be overridden and the provided filter will be returned.
     * If the cached filter has a higher or equal version, the cached filter will be returned.
     *
     * @param filter the Bucket4JConfiguration to find or update in the cache. The id of this filter should not be null.
     * @return returns the Bucket4JConfiguration with the highest version, either the cached or provided filter.
     */
    protected Bucket4JConfiguration getOrUpdateConfigurationFromCache(Bucket4JConfiguration filter) {
        //if caching is disabled or if the filter does not have an id, return the provided filter
        if (this.configCacheManager == null || filter.getId() == null) return filter;

        Bucket4JConfiguration cachedFilter = this.configCacheManager.getValue(filter.getId());
        if (cachedFilter != null && cachedFilter.getBucket4JVersionNumber() >= filter.getBucket4JVersionNumber()) {
            return cachedFilter;
        } else {
            this.configCacheManager.setValue(filter.getId(), filter);
        }
        return filter;
    }

    /**
     * Sets the default Values form the {@link Bucket4JBootProperties} for the {@link Bucket4JConfiguration}
     *
     * @param properties which contains the defaults
     * @param filter     where  the defaults should be set
     */
    protected void setDefaults(Bucket4JBootProperties properties, Bucket4JConfiguration filter) {
        rateLimitService.addDefaultMetricTags(properties, filter);
        if (!StringUtils.hasLength(filter.getHttpResponseBody())) {
            filter.setHttpResponseBody(properties.getDefaultHttpResponseBody());
        }
        if (!StringUtils.hasLength(filter.getHttpContentType())) {
            filter.setHttpContentType(properties.getDefaultHttpContentType());
        }
        if (filter.getHttpStatusCode() == null) {
            filter.setHttpStatusCode(properties.getDefaultHttpStatusCode());
        }
    }

}
