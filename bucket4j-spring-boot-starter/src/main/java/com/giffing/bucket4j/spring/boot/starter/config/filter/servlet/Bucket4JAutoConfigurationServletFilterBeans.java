package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Servlet;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4JAutoConfigurationServletFilterBeans {

	@Bean
	@Servlet
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}

}
