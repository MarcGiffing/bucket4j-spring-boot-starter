package com.giffing.bucket4j.spring.boot.starter.config.cache;

import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricBucketListener;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.TokensInheritanceStrategy;

@FunctionalInterface
public interface ProxyManagerWrapper {
	
	ConsumptionProbeHolder tryConsumeAndReturnRemaining(
			String key,
			Integer numTokens,
			BucketConfiguration bucketConfiguration,
			MetricBucketListener metricBucketListener,
			long configVersion,
			TokensInheritanceStrategy strategy);
}
