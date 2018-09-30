package com.giffing.bucket4j.spring.boot.starter.context.metrics;

public interface MetricHandler {

    void onConsumed(String name, long tokens, String[] tags);

    void onRejected(String name, long tokens, String[] tags);
	
}
