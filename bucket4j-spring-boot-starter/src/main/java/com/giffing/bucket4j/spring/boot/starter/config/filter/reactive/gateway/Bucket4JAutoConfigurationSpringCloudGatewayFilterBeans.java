package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;

@Configuration
public class Bucket4JAutoConfigurationSpringCloudGatewayFilterBeans {

	@Bean
	@Qualifier("GATEWAY")
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
