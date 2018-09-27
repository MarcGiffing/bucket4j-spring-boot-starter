package com.giffing.bucket4j.spring.boot.starter.springboot2.actuator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import com.giffing.bucket4j.spring.boot.starter.context.MetricBucketListener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;

@Configuration
@ConditionalOnClass(value = {Metrics.class})
public class Bucket4jMetrics implements ApplicationListener<ApplicationReadyEvent> {

	@Autowired
	private List<MetricBucketListener> metricBucketListeners = new ArrayList<>();

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		metricBucketListeners.forEach(metricBucketListener -> {
			Counter counterConsumed = Metrics.counter("bucket4j_counter_consumed_" + metricBucketListener.getName());
			Counter counterRejected = Metrics.counter("bucket4j_counter_rejected_" + metricBucketListener.getName());
			
			metricBucketListener.setConsumedFunction( (tokens) -> {
				counterConsumed.increment(tokens);
				Metrics.summary("bucket4j_summary_consumed_" + metricBucketListener.getName());
			});
			metricBucketListener.setRejectedFunction( (tokens) -> {
				counterRejected.increment(tokens);
				Metrics.summary("bucket4j_summary_rejected_" + metricBucketListener.getName());
				
			});
		});
		
	}
	
	
	
}
