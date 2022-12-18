package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.time.temporal.ChronoUnit;

import lombok.Data;

/**
 * Configures the rate of data which should be transfered
 *
 */
@Data
public class BandWidth {

	private long capacity;
	private long time;
	private ChronoUnit unit;

	private long fixedRefillInterval = 0;
	private ChronoUnit fixedRefillIntervalUnit = ChronoUnit.MINUTES;

}
