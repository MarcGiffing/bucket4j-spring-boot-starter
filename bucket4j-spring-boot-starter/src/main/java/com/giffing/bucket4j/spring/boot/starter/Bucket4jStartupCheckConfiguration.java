package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.exception.NoCacheConfiguredException;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnBucket4jEnabled
@AutoConfigureAfter( value = {
        CacheAutoConfiguration.class,
        Bucket4jCacheConfiguration.class
})
@RequiredArgsConstructor
@EnableConfigurationProperties(Bucket4JBootProperties.class)
public class Bucket4jStartupCheckConfiguration {

    private final Bucket4JBootProperties properties;

    @Nullable
    private final SyncCacheResolver syncCacheResolver;

    @Nullable
    private final AsyncCacheResolver asyncCacheResolver;

    @PostConstruct
    public void checkCache() {
        // Either a sync or an async cache resolver must be provided
        if(syncCacheResolver == null && asyncCacheResolver == null) {
            throw new NoCacheConfiguredException(properties.getCacheToUse());
        }

        var filterMethods = getAllFilterMethods();
        checkCacheResolverForFiltersIsRequired(syncCacheResolver == null, filterMethods, Set.of(FilterMethod.SERVLET));
        checkCacheResolverForFiltersIsRequired(asyncCacheResolver == null, filterMethods, Set.of(FilterMethod.WEBFLUX, FilterMethod.GATEWAY));
    }

    private Set<FilterMethod> getAllFilterMethods() {
        return properties.getFilters()
                .stream()
                .map(Bucket4JConfiguration::getFilterMethod)
                .collect(Collectors.toSet());
    }

    private void checkCacheResolverForFiltersIsRequired(boolean cacheResolverNotExists, Set<FilterMethod> filterMethodsToCheck, Set<FilterMethod> configuredFilterMethods) {
        var filterFound = configuredFilterMethods.stream().anyMatch(filterMethodsToCheck::contains);
        // if a servlet filter is configured but no syncCacheResolver found
        if (filterFound && cacheResolverNotExists) {
            throw new NoCacheConfiguredException(properties.getCacheToUse());
        }
    }
}