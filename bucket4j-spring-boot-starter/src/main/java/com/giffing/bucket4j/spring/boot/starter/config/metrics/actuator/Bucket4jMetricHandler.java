package com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

import io.micrometer.core.instrument.Metrics;

@Component
@Primary
public class Bucket4jMetricHandler implements MetricHandler {

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {
		
		List<String> extendedTags = new ArrayList<>();
		extendedTags.add("name");
		extendedTags.add(name);
		
		tags
			.stream()
			.filter(tag -> tag.getTypes().contains(type))
			.forEach(metricTagResult -> {
				extendedTags.add(metricTagResult.getKey());
				extendedTags.add(metricTagResult.getValue());
			});
		
		String[] extendedTagsArray = extendedTags.toArray(new String[0]);

		switch(type) {
		case CONSUMED_COUNTER:
			Metrics
				.counter("bucket4j_summary_consumed", extendedTagsArray)
				.increment(tokens);
			break;
		case REJECTED_COUNTER:
			Metrics
			.counter("bucket4j_summary_rejected", extendedTagsArray)
			.increment(tokens);
			break;
		default:
			throw new IllegalStateException("Unsupported metric type: " + type);
		}
		
	}

}
