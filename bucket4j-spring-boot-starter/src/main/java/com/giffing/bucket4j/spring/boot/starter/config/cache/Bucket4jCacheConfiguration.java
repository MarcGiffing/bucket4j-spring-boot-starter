package com.giffing.bucket4j.spring.boot.starter.config.cache;

import java.security.cert.Extension;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast.HazelcastBucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;

@Configuration
@ConditionalOnClass({ Extension.class})
@AutoConfigureAfter(CacheAutoConfiguration.class)
@Import(value = {JCacheBucket4jConfiguration.class, HazelcastBucket4jCacheConfiguration.class})
public class Bucket4jCacheConfiguration {


}
