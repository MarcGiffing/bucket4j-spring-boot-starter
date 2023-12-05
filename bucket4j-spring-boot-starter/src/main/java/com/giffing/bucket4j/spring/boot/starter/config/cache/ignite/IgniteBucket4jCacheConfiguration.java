package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

import org.apache.ignite.Ignite;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@ConditionalOnClass({ Ignite.class })
@ConditionalOnBean(Ignite.class)
@ConditionalOnMissingBean(AsyncCacheResolver.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "ignite", matchIfMissing = true)
public class IgniteBucket4jCacheConfiguration {
	
	private final Ignite ignite;
	
	public IgniteBucket4jCacheConfiguration(Ignite ignite) {
		this.ignite = ignite;
	}
	
	@Bean
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new IgniteCacheResolver(ignite);
	}
}
