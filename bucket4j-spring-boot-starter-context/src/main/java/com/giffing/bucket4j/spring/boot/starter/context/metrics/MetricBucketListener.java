package com.giffing.bucket4j.spring.boot.starter.context.metrics;

import java.util.List;

import io.github.bucket4j.BucketListener;
import lombok.Getter;

/**
 * Marker Interface
 *
 */
public class MetricBucketListener implements BucketListener {

    @Getter
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
		if(allowedTypes.contains(MetricType.PARKED_COUNTER)) {
			metricHandlers.forEach(metricHandler -> metricHandler.handle(MetricType.PARKED_COUNTER, name, 1, tags));
		}
	}

	@Override
	public void onInterrupted(InterruptedException e) {
		if(allowedTypes.contains(MetricType.INTERRUPTED_COUNTER)) {
			metricHandlers.forEach(metricHandler -> metricHandler.handle(MetricType.INTERRUPTED_COUNTER, name, 1, tags));
		}
		
	}

	@Override
	public void onDelayed(long nanos) {
		if(allowedTypes.contains(MetricType.DELAYED_COUNTER)) {
			metricHandlers.forEach(metricHandler -> metricHandler.handle(MetricType.DELAYED_COUNTER, name, 1, tags));
		}
	}

}
