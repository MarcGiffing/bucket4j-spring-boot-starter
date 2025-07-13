package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.webflux;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Webflux;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxRateLimiterFilterFactory;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.impl.DefaultWebfluxRateLimiterFilterFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4JAutoConfigurationWebfluxFilterBeans {

	@Bean
	@Webflux
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}

	@Bean
	@Webflux
	@ConditionalOnMissingBean(WebfluxRateLimiterFilterFactory.class)
	public WebfluxRateLimiterFilterFactory webfluxRateLimiterFilterFactory() {
		return new DefaultWebfluxRateLimiterFilterFactory();
	}
}
