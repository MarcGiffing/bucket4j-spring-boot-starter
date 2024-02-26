package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Gateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
public class Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans {

	@Bean
	@Gateway
	public Bucket4jConfigurationHolder gatewayConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}

	@Bean
	public ExpressionParser gatewayFilterExpressionParser() {
		SpelParserConfiguration config = new SpelParserConfiguration(
				SpelCompilerMode.IMMEDIATE,
				this.getClass().getClassLoader());
		return new SpelExpressionParser(config);
	}
	
}
