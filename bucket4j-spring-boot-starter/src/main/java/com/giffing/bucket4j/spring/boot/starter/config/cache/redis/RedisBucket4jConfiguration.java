package com.giffing.bucket4j.spring.boot.starter.config.cache.redis;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
@ConditionalOnClass(RedisTemplate.class)
@ConditionalOnBean(RedisTemplate.class)
public class RedisBucket4jConfiguration {

	@Bean
	public SyncCacheResolver bucket4RedisResolver(RedisTemplate redisTemplate) {
		return new RedisCacheResolver(redisTemplate);
	}
}
