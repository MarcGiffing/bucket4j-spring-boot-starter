package com.giffing.bucket4j.spring.boot.starter.cache.ignite;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnAsynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager;
import org.apache.ignite.Ignite;
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
@ConditionalOnAsynchronousPropertyCondition
@AutoConfigureAfter({CacheAutoConfiguration.class})
@ConditionalOnClass({Ignite.class})
@ConditionalOnBean(Ignite.class)
@ConditionalOnCache("ignite")
public class IgniteBucket4jCacheConfiguration {

    private final Ignite ignite;
    private final String configCacheName;

    public IgniteBucket4jCacheConfiguration(Ignite ignite, Bucket4JBootProperties properties) {
        this.ignite = ignite;
        this.configCacheName = properties.getFilterConfigCacheName();
    }

    @Bean
    @ConditionalOnMissingBean(AsyncCacheResolver.class)
    public AsyncCacheResolver hazelcastCacheResolver() {
        return new IgniteCacheResolver(ignite);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnFilterConfigCacheEnabled
    public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
        return new IgniteCacheManager<>(ignite.cache(configCacheName));
    }

    @Bean
    @ConditionalOnFilterConfigCacheEnabled
    public IgniteCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
        return new IgniteCacheListener<>(ignite.cache(configCacheName), eventPublisher);
    }
}
