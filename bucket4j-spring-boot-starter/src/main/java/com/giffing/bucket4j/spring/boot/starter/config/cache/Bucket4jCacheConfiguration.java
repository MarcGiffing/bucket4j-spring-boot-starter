package com.giffing.bucket4j.spring.boot.starter.config.cache;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast.HazelcastBucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.InfinispanJCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.redis.RedisBucket4jConfiguration;

@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
@Import(value = {
        JCacheBucket4jConfiguration.class,
        InfinispanJCacheBucket4jConfiguration.class,
        HazelcastBucket4jCacheConfiguration.class,
        RedisBucket4jConfiguration.class
})
public class Bucket4jCacheConfiguration {
}
