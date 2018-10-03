package com.giffing.bucket4j.spring.boot.starter.context.metrics;

import java.util.List;

public interface MetricHandler {

    void onConsumed(String name, long tokens, List<MetricTagResult> tags);

    void onRejected(String name, long tokens, List<MetricTagResult> tags);
	
}
