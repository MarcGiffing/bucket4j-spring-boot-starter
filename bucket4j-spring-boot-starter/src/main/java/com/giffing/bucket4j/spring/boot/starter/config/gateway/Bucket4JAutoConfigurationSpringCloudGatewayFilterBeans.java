package com.giffing.bucket4j.spring.boot.starter.config.gateway;

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
		System.out.println("######################-GATEWAY");
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE,
				this.getClass().getClassLoader());
		ExpressionParser parser = new SpelExpressionParser(config);

		return parser;
	}
	
}
