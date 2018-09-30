package com.giffing.bucket4j.spring.boot.starter.config.metrics;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4jMetricConfiguration {

	@Bean
	public IPMetricTagStrategy ipMetricTagStrategy() {
		return new IPMetricTagStrategy();
	}
	
}
