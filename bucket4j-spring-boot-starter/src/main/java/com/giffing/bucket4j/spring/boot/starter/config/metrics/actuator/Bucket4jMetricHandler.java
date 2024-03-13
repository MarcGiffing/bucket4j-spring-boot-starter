package com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

import io.micrometer.core.instrument.Metrics;

@Component
@Primary
public class Bucket4jMetricHandler implements MetricHandler {

	public static final String METRIC_COUNTER_PREFIX = "bucket4j_summary_";

	@Override
	public void handle(MetricType type, String name, long counterIncrement, List<MetricTagResult> tags) {

		List<String> extendedTags = new ArrayList<>();
		extendedTags.add("name");
		extendedTags.add(name);

		tags.stream().filter(tag -> tag.getTypes().contains(type)).forEach(metricTagResult -> {
			extendedTags.add(metricTagResult.getKey());
			extendedTags.add(metricTagResult.getValue());
		});

		String[] extendedTagsArray = extendedTags.toArray(new String[0]);

		switch (type) {
		case CONSUMED_COUNTER:
			Metrics
				.counter(METRIC_COUNTER_PREFIX + "consumed", extendedTagsArray)
				.increment(counterIncrement);
			break;
		case REJECTED_COUNTER:
			Metrics
				.counter(METRIC_COUNTER_PREFIX + "rejected", extendedTagsArray)
				.increment(counterIncrement);
			break;
		case PARKED_COUNTER:
			Metrics
					.counter(METRIC_COUNTER_PREFIX + "parked", extendedTagsArray)
					.increment(counterIncrement);
			break;
		case INTERRUPTED_COUNTER:
		Metrics
				.counter(METRIC_COUNTER_PREFIX + "interrupted", extendedTagsArray)
				.increment(counterIncrement);
		break;
		case DELAYED_COUNTER:
			Metrics
					.counter(METRIC_COUNTER_PREFIX + "delayed", extendedTagsArray)
					.increment(counterIncrement);
			break;
		default:
			throw new IllegalStateException("Unsupported metric type: " + type);
		}

	}

}
