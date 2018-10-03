package com.giffing.bucket4j.spring.boot.starter.springboot2.actuator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;

import io.micrometer.core.instrument.Metrics;

@Component
@Primary
public class Bucket4jMetricHandler implements MetricHandler {

	@Override
	public void onConsumed(String name, long tokens, List<MetricTagResult> tags) {
		List<String> extendedTags = new ArrayList<>();
		extendedTags.add("name");
		extendedTags.add(name);
		tags.forEach(metricTagResult -> {
			extendedTags.add(metricTagResult.getKey());
			extendedTags.add(metricTagResult.getValue());
		});
		Metrics
			.counter("bucket4j_summary_consumed", extendedTags.toArray(new String[0]))
			.increment(tokens);
	}

	@Override
	public void onRejected(String name, long tokens, List<MetricTagResult> tags) {
		List<String> extendedTags = new ArrayList<>();
		extendedTags.add("name");
		extendedTags.add(name);
		tags.forEach(metricTagResult -> {
			extendedTags.add(metricTagResult.getKey());
			extendedTags.add(metricTagResult.getValue());
		});
		Metrics
			.counter("bucket4j_summary_rejected", extendedTags.toArray(new String[0]))
			.increment(tokens);
	}

}
