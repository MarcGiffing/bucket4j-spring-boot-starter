package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import io.github.bucket4j.redis.jedis.cas.JedisBasedProxyManager.JedisBasedProxyManagerBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(JedisBasedProxyManagerBuilder.class)
@ConditionalOnBean(JedisPool.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "redis-jedis", matchIfMissing = true)
public class JedisBucket4jConfiguration {

	@Bean
	public SyncCacheResolver bucket4RedisResolver(JedisPool jedisPool) {
		return new JedisCacheResolver(jedisPool);
	}
}
