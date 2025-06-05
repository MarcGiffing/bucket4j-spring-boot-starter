package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Gateway;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.GatewayRateLimitFilterFactory;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.impl.DefaultGatewayRateLimitWebFilterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans {

	@Bean
	@Gateway
	public Bucket4jConfigurationHolder gatewayConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}


	@Bean
	@Gateway
	@ConditionalOnMissingBean(GatewayRateLimitFilterFactory.class)
	public GatewayRateLimitFilterFactory gatewayRateLimitFilterFactory() {
		return new DefaultGatewayRateLimitWebFilterFactory();
	}
}
