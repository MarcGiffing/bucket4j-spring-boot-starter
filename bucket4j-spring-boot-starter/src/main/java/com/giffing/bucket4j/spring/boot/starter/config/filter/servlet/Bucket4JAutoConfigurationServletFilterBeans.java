package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Servlet;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.ServletRateLimiterFilterFactory;
import com.giffing.bucket4j.spring.boot.starter.filter.servlet.impl.DefaultServletRateLimiterFilterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4JAutoConfigurationServletFilterBeans {

	@Bean
	@Servlet
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}

	@Bean
	@Servlet
	@ConditionalOnMissingBean(ServletRateLimiterFilterFactory.class)
	public ServletRateLimiterFilterFactory servletRateLimiterFilterFactory() {
		return new DefaultServletRateLimiterFilterFactory();
	}
}
