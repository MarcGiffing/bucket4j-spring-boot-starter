package com.giffing.bucket4j.spring.boot.starter.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnSynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import javax.cache.Caching;

/**
 * The configuration class for Infinispan. Infinispan is not directly supported by
 * bucket4j. See {@link InfinispanCacheResolver} for more informations.
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBucket4jEnabled
@AutoConfigureAfter({CacheAutoConfiguration.class})
@ConditionalOnSynchronousPropertyCondition
@ConditionalOnClass({CacheContainer.class, Caching.class, JCacheCacheManager.class})
@ConditionalOnBean(CacheContainer.class)
@ConditionalOnCache("jcache-infinispan")
public class InfinispanJCacheBucket4jConfiguration {

    private final CacheContainer cacheContainer;
    private final String configCacheName;

    public InfinispanJCacheBucket4jConfiguration(CacheContainer cacheContainer, Bucket4JBootProperties properties) {
        this.cacheContainer = cacheContainer;
        this.configCacheName = properties.getFilterConfigCacheName();
    }

    @Bean
    @ConditionalOnMissingBean(SyncCacheResolver.class)
    public SyncCacheResolver bucket4jCacheResolver() {
        return new InfinispanJCacheCacheResolver(cacheContainer);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnFilterConfigCacheEnabled
    public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
        return new InfinispanCacheManager<>(cacheContainer.getCache(configCacheName));
    }

    @Bean
    @ConditionalOnFilterConfigCacheEnabled
    public InfinispanCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
        return new InfinispanCacheListener<>(cacheContainer.getCache(configCacheName), eventPublisher);
    }
}
