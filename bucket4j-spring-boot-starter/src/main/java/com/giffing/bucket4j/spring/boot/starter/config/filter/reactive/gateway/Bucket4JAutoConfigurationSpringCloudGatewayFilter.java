package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway;

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
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParser;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.SpringCloudGatewayRateLimitFilter;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.gateway.config.GatewayAutoConfiguration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Configures Servlet Filters for Bucket4Js rate limit.
 */
@Configuration
@ConditionalOnBucket4jEnabled
@ConditionalOnClass({GlobalFilter.class})
@EnableConfigurationProperties({Bucket4JBootProperties.class})
@AutoConfigureBefore(GatewayAutoConfiguration.class)
@AutoConfigureAfter(value = {CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class})
@ConditionalOnBean(value = AsyncCacheResolver.class)
@Import(value = {ServiceConfiguration.class, WebfluxExecutePredicateConfiguration.class, SpringBootActuatorConfig.class, Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans.class})
@Slf4j
public class Bucket4JAutoConfigurationSpringCloudGatewayFilter extends Bucket4JBaseConfiguration<ServerHttpRequest, ServerHttpResponse> {

    private final Bucket4JBootProperties properties;

    private final GenericApplicationContext context;

    private final AsyncCacheResolver cacheResolver;

    private final Bucket4jConfigurationHolder gatewayConfigurationHolder;

    public Bucket4JAutoConfigurationSpringCloudGatewayFilter(
            Bucket4JBootProperties properties,
            GenericApplicationContext context,
            AsyncCacheResolver cacheResolver,
            List<MetricHandler> metricHandlers,
            List<ExecutePredicate<ServerHttpRequest>> executePredicates,
            Bucket4jConfigurationHolder gatewayConfigurationHolder,
            RateLimitService rateLimitService,
            UrlPatternParser urlPatternParser,
            @Autowired(required = false) CacheManager<String, Bucket4JConfiguration> configCacheManager) {

        super(
                rateLimitService,
                configCacheManager,
                metricHandlers,
                executePredicates
                        .stream()
                        .collect(Collectors.toMap(ExecutePredicate::name, Function.identity())),
                urlPatternParser);
        this.properties = properties;
        this.context = context;
        this.cacheResolver = cacheResolver;
        this.gatewayConfigurationHolder = gatewayConfigurationHolder;
        initFilters();
    }


    public void initFilters() {
        AtomicInteger filterCount = new AtomicInteger(0);
        properties
                .getFilters()
                .stream()
                .filter(filter -> StringUtils.hasText(filter.getUrlPattern()) && filter.getFilterMethod().equals(FilterMethod.GATEWAY))
                .map(filter -> properties.isFilterConfigCachingEnabled() ? getOrUpdateConfigurationFromCache(filter) : filter)
                .forEach(filter -> {
                    setDefaults(properties, filter);
                    filterCount.incrementAndGet();
                    var filterConfig = buildFilterConfig(filter, cacheResolver.resolve(filter.getCacheName()));

                    gatewayConfigurationHolder.addFilterConfiguration(filter);

                    //Use either the filter id as bean name or the prefix + counter if no id is configured
                    String beanName = filter.getId() != null ? filter.getId() : ("bucket4JGatewayFilter" + filterCount);
                    context.registerBean(beanName, GlobalFilter.class, () -> new SpringCloudGatewayRateLimitFilter(filterConfig));

                    log.info("create-gateway-filter;{};{};{}", filterCount, filter.getCacheName(), filter.getUrlPattern());
                });
    }

    @Override
    public void onCacheUpdateEvent(CacheUpdateEvent<String, Bucket4JConfiguration> event) {
        //only handle gateway filter updates
        Bucket4JConfiguration newConfig = event.getNewValue();
        if (newConfig.getFilterMethod().equals(FilterMethod.GATEWAY)) {
            try {
                SpringCloudGatewayRateLimitFilter filter = context.getBean(event.getKey(), SpringCloudGatewayRateLimitFilter.class);
                var newFilterConfig = buildFilterConfig(newConfig, cacheResolver.resolve(newConfig.getCacheName()));
                filter.setFilterConfig(newFilterConfig);
            } catch (Exception exception) {
                log.warn("Failed to update Gateway Filter configuration. {}", exception.getMessage());
            }
        }
    }
}
