package com.giffing.bucket4j.spring.boot.starter.config.metrics;

import java.util.List;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;

@Component
public class DummyMetricHandler implements MetricHandler {

	@Override
	public void onConsumed(String name, long tokens, List<MetricTagResult> tags) {}

	@Override
	public void onRejected(String name, long tokens, List<MetricTagResult> tags) {}

}
