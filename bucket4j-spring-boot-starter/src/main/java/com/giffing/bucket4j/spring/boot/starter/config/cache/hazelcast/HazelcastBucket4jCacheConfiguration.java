package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@ConditionalOnClass({ HazelcastInstance.class })
@ConditionalOnBean(HazelcastInstance.class)
public class HazelcastBucket4jCacheConfiguration {
	
	private HazelcastInstance hazelcastInstance;
	
	public HazelcastBucket4jCacheConfiguration(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}
	
	@Bean
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new HazelcastCacheResolver(hazelcastInstance);
	}
}
