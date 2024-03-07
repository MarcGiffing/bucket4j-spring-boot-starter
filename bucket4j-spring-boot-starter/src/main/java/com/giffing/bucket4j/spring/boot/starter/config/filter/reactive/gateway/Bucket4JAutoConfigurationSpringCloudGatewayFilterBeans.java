package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Gateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans {

	@Bean
	@Gateway
	public Bucket4jConfigurationHolder gatewayConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}


}
