package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import org.apache.ignite.Ignite;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

@Configuration
@ConditionalOnWebApplication(type = Type.REACTIVE)
@ConditionalOnClass({ Ignite.class })
@ConditionalOnBean(Ignite.class)
@ConditionalOnMissingBean(AsyncCacheResolver.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", havingValue = "ignite", matchIfMissing = true)
public class IgniteBucket4jCacheConfiguration {
	
	private Ignite ignite;
	
	public IgniteBucket4jCacheConfiguration(Ignite ignite) {
		this.ignite = ignite;
	}
	
	@Bean
	public AsyncCacheResolver hazelcastCacheResolver() {
		return new IgniteCacheResolver(ignite);
	}
}
