package com.giffing.bucket4j.spring.boot.starter.config.metrics;

import java.util.List;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

@Component
public class DummyMetricHandler implements MetricHandler {

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {}


}
