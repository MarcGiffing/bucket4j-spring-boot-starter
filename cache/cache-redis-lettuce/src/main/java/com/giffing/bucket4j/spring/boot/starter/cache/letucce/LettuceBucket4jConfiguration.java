package com.giffing.bucket4j.spring.boot.starter.cache.letucce;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnAsynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.core.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBucket4jEnabled
@ConditionalOnAsynchronousPropertyCondition
@ConditionalOnClass(LettuceBasedProxyManager.class)
@ConditionalOnBean(RedisClient.class)
@ConditionalOnCache("redis-lettuce")
public class LettuceBucket4jConfiguration {

    private final RedisClient redisClient;
    private final String configCacheName;

    public LettuceBucket4jConfiguration(RedisClient redisClient, Bucket4JBootProperties properties) {
        this.redisClient = redisClient;
        this.configCacheName = properties.getFilterConfigCacheName();
    }

    @Bean
    @ConditionalOnMissingBean(AsyncCacheResolver.class)
    public AsyncCacheResolver bucket4RedisResolver() {
        return new LettuceCacheResolver(this.redisClient);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    @ConditionalOnFilterConfigCacheEnabled
    public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
        return new LettuceCacheManager<>(redisClient, configCacheName, Bucket4JConfiguration.class);
    }

    @Bean
    @ConditionalOnFilterConfigCacheEnabled
    public LettuceCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
        return new LettuceCacheListener<>(redisClient, configCacheName, String.class, Bucket4JConfiguration.class, eventPublisher);
    }
}
