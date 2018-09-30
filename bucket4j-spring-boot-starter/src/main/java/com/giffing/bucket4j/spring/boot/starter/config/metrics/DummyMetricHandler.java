package com.giffing.bucket4j.spring.boot.starter.config.metrics;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;

@Component
public class DummyMetricHandler implements MetricHandler {

	@Override
	public void onConsumed(String name, long tokens, String[] tags) {}

	@Override
	public void onRejected(String name, long tokens, String[] tags) {}

}
