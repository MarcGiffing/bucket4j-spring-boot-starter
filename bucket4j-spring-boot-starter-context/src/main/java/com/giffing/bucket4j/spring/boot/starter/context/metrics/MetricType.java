package com.giffing.bucket4j.spring.boot.starter.context.metrics;

public enum MetricType {

	/**
	 * Count token consumption.
	 */
	CONSUMED_COUNTER,

	/**
	 * Count whenever consumption request for tokens is rejected.
	 */
	REJECTED_COUNTER,

	/**
	 * Count parked threads which wait of tokens refill in result of interaction with Bucket4js BlockingBucket.
	 */
	PARKED_COUNTER,

	/**
	 *  Count interrupted threads during the wait of tokens refill in result of interaction with Bucket4js BlockingBucket
	 */
	INTERRUPTED_COUNTER,

	/**
	 * Count delayed tasks was submit to java.util.concurrent.ScheduledExecutorService
	 * because of wait for tokens refill in result of interaction with SchedulingBucket
	 */
	DELAYED_COUNTER
}
