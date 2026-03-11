package com.giffing.bucket4j.spring.boot.starter.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnSynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
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

/**
 * Configures the asynchronous support for Hazelcast. The synchronous support of Hazelcast
 * is already provided by the {JCacheBucket4jConfiguration}. It uses the {@link HazelcastInstance}
 * to access the {@link HazelcastInstance} to retrieve the cache.
 */
@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBucket4jEnabled
@ConditionalOnSynchronousPropertyCondition
@AutoConfigureAfter({CacheAutoConfiguration.class})
@ConditionalOnClass({HazelcastInstance.class})
@ConditionalOnBean(HazelcastInstance.class)
@ConditionalOnCache("hazelcast-spring")
public class HazelcastSpringBucket4jCacheConfiguration {

    private final HazelcastInstance hazelcastInstance;
    private final String configCacheName;

    public HazelcastSpringBucket4jCacheConfiguration(HazelcastInstance hazelcastInstance, Bucket4JBootProperties properties) {
        this.hazelcastInstance = hazelcastInstance;
        this.configCacheName = properties.getFilterConfigCacheName();
    }

    @Bean
    @ConditionalOnMissingBean(SyncCacheResolver.class)
    public SyncCacheResolver hazelcastCacheResolver() {
        return new HazelcastCacheResolver(hazelcastInstance, false);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnFilterConfigCacheEnabled
    public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
        IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(configCacheName);
        return new com.giffing.bucket4j.spring.boot.starter.cache.hazelcast.HazelcastCacheManager<>(map);
    }

    @Bean
    @ConditionalOnFilterConfigCacheEnabled
    public HazelcastCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
        IMap<String, Bucket4JConfiguration> map = hazelcastInstance.getMap(configCacheName);
        return new HazelcastCacheListener<>(map, eventPublisher);
    }

}
