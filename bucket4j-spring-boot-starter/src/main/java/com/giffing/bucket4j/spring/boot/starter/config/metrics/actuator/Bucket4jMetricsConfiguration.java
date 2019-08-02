package com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;

import io.micrometer.core.instrument.Metrics;

@Configuration
@ConditionalOnClass(value = {Metrics.class})
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX + ".metrics", value = { "enabled" }, matchIfMissing = true)
public class Bucket4jMetricsConfiguration {

	@Bean
	@Primary
	public MetricHandler springBoot2Bucket4jMetricHandler() {
		return new Bucket4jMetricHandler();
	}
	
}
