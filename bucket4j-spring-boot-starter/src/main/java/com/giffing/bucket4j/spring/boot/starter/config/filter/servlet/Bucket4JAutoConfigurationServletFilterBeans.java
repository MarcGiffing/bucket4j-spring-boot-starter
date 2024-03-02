package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Servlet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
public class Bucket4JAutoConfigurationServletFilterBeans {

	@Bean
	@Servlet
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}
	
	@Bean
	public ExpressionParser servletFilterExpressionParser() {
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader());
		return new SpelExpressionParser(config);
	}
	
}
