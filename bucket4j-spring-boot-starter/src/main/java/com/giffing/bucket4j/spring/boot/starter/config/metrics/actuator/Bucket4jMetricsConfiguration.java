package com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator;

import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import io.micrometer.core.instrument.Metrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@ConditionalOnClass(value = {Metrics.class})
@ConditionalOnBucket4jEnabled
public class Bucket4jMetricsConfiguration {

	@Bean
	@Primary
	public MetricHandler springBoot2Bucket4jMetricHandler() {
		return new Bucket4jMetricHandler();
	}
	
}
