package com.giffing.bucket4j.spring.boot.starter.context.metrics;

import java.util.List;

import io.github.bucket4j.BucketListener;

/**
 * Marker Interface
 *
 */
public class MetricBucketListener implements BucketListener {

    private final String name;
	private final List<MetricTagResult> tags;
	private final List<MetricHandler> metricHandlers;
	private final List<MetricType> allowedTypes;

    public MetricBucketListener(String name, List<MetricHandler> metricHandlers, List<MetricType> allowedTypes, List<MetricTagResult> tags) {
		this.name = name;
		this.metricHandlers = metricHandlers;
		this.allowedTypes = allowedTypes;
		this.tags = tags;
	}
    
    @Override
    public void onConsumed(long tokens) {
    	if(allowedTypes.contains(MetricType.CONSUMED_COUNTER)) {
    		metricHandlers.forEach(metricHandler -> metricHandler.handle(MetricType.CONSUMED_COUNTER, name, tokens, tags));
    	}
    }

    @Override
    public void onRejected(long tokens) {
    	if(allowedTypes.contains(MetricType.REJECTED_COUNTER)) {
    		metricHandlers.forEach(metricHandler -> metricHandler.handle(MetricType.REJECTED_COUNTER, name, tokens, tags));
    	}
    }

    @Override
    public void onParked(long nanos) {
    }

    public long getConsumed() {
        return 0;
    }

    public long getRejected() {
        return 0;
    }

    public long getParkedNanos() {
        return 0;
    }

    public long getInterrupted() {
        return 0;
    }

	@Override
	public void onInterrupted(InterruptedException e) {
	}

	@Override
	public void onDelayed(long nanos) {
	}

	public String getName() {
		return name;
	}

}
