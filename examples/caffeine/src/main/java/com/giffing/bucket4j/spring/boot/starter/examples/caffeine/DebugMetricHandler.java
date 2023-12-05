package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

@Component
public class DebugMetricHandler implements MetricHandler {

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {
	System.out.printf("type: %s; name: %s; tags: %s; tokens: %s%n",
				type,
				name,
				tags
					.stream()
					.map(mtr -> mtr.getKey() + ":" + mtr.getValue())
					.collect(Collectors.joining(",")),
				tokens);
		
	}

}
