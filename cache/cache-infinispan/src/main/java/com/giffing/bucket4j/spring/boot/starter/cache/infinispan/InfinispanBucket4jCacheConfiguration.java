package com.giffing.bucket4j.spring.boot.starter.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnAsynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager;
import org.infinispan.manager.CacheContainer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBucket4jEnabled
@AutoConfigureAfter({CacheAutoConfiguration.class})
@ConditionalOnAsynchronousPropertyCondition
@ConditionalOnClass({CacheContainer.class})
@ConditionalOnBean(CacheContainer.class)
@ConditionalOnMissingBean(AsyncCacheResolver.class)
@ConditionalOnCache("infinispan")
public class InfinispanBucket4jCacheConfiguration {

    private final CacheContainer cacheContainer;
    private final String configCacheName;

    public InfinispanBucket4jCacheConfiguration(CacheContainer cacheContainer, Bucket4JBootProperties properties) {
        this.cacheContainer = cacheContainer;
        this.configCacheName = properties.getFilterConfigCacheName();
    }

    @Bean
    public AsyncCacheResolver infinispanCacheResolver() {
        return new InfinispanCacheResolver(cacheContainer);
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
