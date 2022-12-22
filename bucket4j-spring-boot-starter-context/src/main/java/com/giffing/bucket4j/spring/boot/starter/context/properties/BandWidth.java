package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.time.temporal.ChronoUnit;

import com.giffing.bucket4j.spring.boot.starter.context.RefillSpeed;

import lombok.Data;

/**
 * Configures the rate of data which should be transfered
 *
 */
@Data
public class BandWidth {

	private long capacity;
	
	private Long refillCapacity;
	
	private long time;
	
	private ChronoUnit unit;
	
	private Long initialCapacity;
	
	private RefillSpeed refillSpeed = RefillSpeed.GREEDY;
	
}
