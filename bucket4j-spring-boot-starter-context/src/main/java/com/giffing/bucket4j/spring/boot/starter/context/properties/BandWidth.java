package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.context.RefillSpeed;
import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.ValidDurationChronoUnit;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * Configures the rate of data which should be transfered
 *
 */
@Data
public class BandWidth implements Serializable {

	private String id;

	public void setId(String id) {
		if(StringUtils.hasText(id)){
			this.id = id.trim();
		}
	}

	@Positive
	private long capacity;

	@Min(1)
	private Long refillCapacity;
	
	@Positive
	private long time;
	
	@NotNull
	@ValidDurationChronoUnit
	private ChronoUnit unit;
	
	@Min(1)
	private Long initialCapacity;
	
	@NotNull
	private RefillSpeed refillSpeed = RefillSpeed.GREEDY;
	
}
