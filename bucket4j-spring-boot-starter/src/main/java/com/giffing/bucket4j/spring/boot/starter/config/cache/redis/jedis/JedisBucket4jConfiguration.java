package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnFilterConfigCacheEnabled;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnSynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager.JedisBasedProxyManagerBuilder;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
@ConditionalOnSynchronousPropertyCondition
@ConditionalOnClass(JedisBasedProxyManagerBuilder.class)
@ConditionalOnBean(JedisPool.class)
@ConditionalOnCache("redis-jedis")
public class JedisBucket4jConfiguration {

	public final JedisPool jedisPool;
	private final String configCacheName;

	public JedisBucket4jConfiguration(JedisPool jedisPool, Bucket4JBootProperties properties) {
		this.jedisPool = jedisPool;
		this.configCacheName = properties.getFilterConfigCacheName();
	}

	@Bean
	@ConditionalOnMissingBean(SyncCacheResolver.class)
	public SyncCacheResolver bucket4RedisResolver() {
		return new JedisCacheResolver(jedisPool);
	}

	@Bean
	@ConditionalOnMissingBean(CacheManager.class)
	@ConditionalOnFilterConfigCacheEnabled
	public CacheManager<String, Bucket4JConfiguration> configCacheManager() {
		return new JedisCacheManager<>(jedisPool, configCacheName, Bucket4JConfiguration.class);
	}

	@Bean
	@ConditionalOnFilterConfigCacheEnabled
	public JedisCacheListener<String, Bucket4JConfiguration> configCacheListener(ApplicationEventPublisher eventPublisher) {
		return new JedisCacheListener<>(jedisPool, configCacheName, String.class, Bucket4JConfiguration.class, eventPublisher);
	}
}
