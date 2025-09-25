package com.giffing.bucket4j.spring.boot.starter.test.aop;


import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

@ConditionalOnBucket4jEnabled
@ConditionalOnBean(value = SyncCacheResolver.class)
@TestConfiguration
public class Bucket4JAnnotationTestAutoconfiguration {

    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new
                CaffeineCacheManager();
        cacheManager.setCaffeine(caffeine);

        return cacheManager;
    }

    @Bean
    Caffeine<Object, Object> caffeineSpec() {
        return Caffeine.newBuilder()
                .initialCapacity(10)
                .maximumSize(100)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .executor(Runnable::run);
    }
}
