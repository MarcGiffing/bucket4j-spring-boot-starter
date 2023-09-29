package com.giffing.bucket4j.spring.boot.starter.config.cache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan.InfinispanBucket4jCacheConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast.HazelcastReactiveBucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast.HazelcastSpringBucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.InfinispanJCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis.JedisBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce.LettuceBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson.RedissonBucket4jConfiguration;

@Configuration
@AutoConfigureAfter(CacheAutoConfiguration.class)
@Import(value = {
        JCacheBucket4jConfiguration.class,
        InfinispanJCacheBucket4jConfiguration.class,
        InfinispanBucket4jCacheConfiguration.class,
        HazelcastReactiveBucket4jCacheConfiguration.class,
        HazelcastSpringBucket4jCacheConfiguration.class,
        JedisBucket4jConfiguration.class,
        LettuceBucket4jConfiguration.class,
        RedissonBucket4jConfiguration.class,
})
public class Bucket4jCacheConfiguration {
}
