package com.giffing.bucket4j.spring.boot.starter.config.cache;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResultWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricBucketListener;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.TokensInheritanceStrategy;

@FunctionalInterface
public interface ProxyManagerWrapper {

	RateLimitResultWrapper tryConsumeAndReturnRemaining(
			String key,
			Integer numTokens,
			boolean isEstimation,
			BucketConfiguration bucketConfiguration,
			MetricBucketListener metricBucketListener,
			long configVersion,
			TokensInheritanceStrategy strategy);
}
