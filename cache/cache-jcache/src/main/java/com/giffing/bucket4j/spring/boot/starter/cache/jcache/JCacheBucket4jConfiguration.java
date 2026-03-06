package com.giffing.bucket4j.spring.boot.starter.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnSynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import javax.cache.CacheManager;
import javax.cache.Caching;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBucket4jEnabled
@ConditionalOnSynchronousPropertyCondition
@AutoConfigureAfter(CacheAutoConfiguration.class)
@ConditionalOnClass({Caching.class, JCacheCacheManager.class})
@ConditionalOnBean(CacheManager.class)
@ConditionalOnCache("jcache")
@EnableConfigurationProperties({Bucket4JBootProperties.class})
public class JCacheBucket4jConfiguration {

    private final CacheManager cacheManager;
    private final String configCacheName;

    public JCacheBucket4jConfiguration(CacheManager cacheManager, Bucket4JBootProperties properties) {
        this.cacheManager = cacheManager;
        this.configCacheName = properties.getFilterConfigCacheName();
    }

    @Bean
    @ConditionalOnMissingBean(SyncCacheResolver.class)
    public SyncCacheResolver bucket4jCacheResolver() {
        return new JCacheCacheResolver(cacheManager);
    }

    @Bean
    @ConditionalOnMissingBean(com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager.class)
    @ConditionalOnFilterConfigCacheEnabled
    public com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager<String, Bucket4JConfiguration> configCacheManager(
    ) {
        return new com.giffing.bucket4j.spring.boot.starter.cache.jcache.JCacheCacheManager<>(cacheManager.getCache(configCacheName));
    }

    @Bean
    @ConditionalOnFilterConfigCacheEnabled
    public JCacheCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
        return new JCacheCacheListener<>(cacheManager.getCache(configCacheName), eventPublisher);
    }

}
