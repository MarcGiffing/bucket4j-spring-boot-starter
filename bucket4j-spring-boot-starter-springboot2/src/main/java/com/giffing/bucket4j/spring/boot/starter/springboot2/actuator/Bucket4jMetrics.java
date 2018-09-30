package com.giffing.bucket4j.spring.boot.starter.springboot2.actuator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;

import io.micrometer.core.instrument.Metrics;

@Configuration
@ConditionalOnClass(value = {Metrics.class})
public class Bucket4jMetrics {

	@Bean
	@Primary
	public MetricHandler springBoot2Bucket4jMetricHandler() {
		return new Bucket4jMetricHandler();
	}
	
}
