package com.giffing.bucket4j.spring.boot.starter.context;

import java.time.temporal.ChronoUnit;

/**
 * Configures the rate of data which should be transfered
 *
 */
public class BandWidthConfig {


	private long capacity;
	private long time;
	private ChronoUnit unit;

	private long fixedRefillInterval = 0;;
	private ChronoUnit fixedRefillIntervalUnit = ChronoUnit.MINUTES;

	public long getCapacity() {
		return capacity;
	}
	public void setCapacity(long capacity) {
		this.capacity = capacity;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	public ChronoUnit getUnit() {
		return unit;
	}
	public void setUnit(ChronoUnit unit) {
		this.unit = unit;
	}
	public long getFixedRefillInterval() {
		return fixedRefillInterval;
	}
	public void setFixedRefillInterval(long fixedRefillInterval) {
		this.fixedRefillInterval = fixedRefillInterval;
	}
	public ChronoUnit getFixedRefillIntervalUnit() {
		return fixedRefillIntervalUnit;
	}
	public void setFixedRefillIntervalUnit(ChronoUnit fixedRefillIntervalUnit) {
		this.fixedRefillIntervalUnit = fixedRefillIntervalUnit;
	}

}
