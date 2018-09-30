package com.giffing.bucket4j.spring.boot.starter.config.metrics;

import javax.servlet.ServletRequest;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagStrategy;

public class IPMetricTagStrategy implements MetricTagStrategy<ServletRequest> {

	@Override
	public MetricTagResult getTags(ServletRequest request) {
		return new MetricTagResult(key(), request.getRemoteAddr());
	}

	@Override
	public String key() {
		return "IP";
	}

	@Override
	public boolean supports(Object request) {
		return request instanceof ServletRequest;
	}

}
