package com.giffing.bucket4j.spring.boot.starter.springboot2.actuator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;

import io.micrometer.core.instrument.Metrics;

@Component
@Primary
public class Bucket4jMetricHandler implements MetricHandler {

	@Override
	public void onConsumed(String name, long tokens, String[] tags) {
		List<String> extendedTags = new ArrayList<>();
		extendedTags.add("name");
		extendedTags.add(name);
		extendedTags.addAll(Arrays.asList(tags));
		System.out.println(tokens);
		Metrics
			.counter("bucket4j_summary_consumed", extendedTags.toArray(new String[0]))
			.increment(tokens);
	}

	@Override
	public void onRejected(String name, long tokens, String[] tags) {
		List<String> extendedTags = new ArrayList<>();
		extendedTags.add("name");
		extendedTags.add(name);
		extendedTags.addAll(Arrays.asList(tags));
		System.out.println(tokens);
		Metrics
			.counter("bucket4j_summary_rejected", extendedTags.toArray(new String[0]))
			.increment(tokens);
	}

}
