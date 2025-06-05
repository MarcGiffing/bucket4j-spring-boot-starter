package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.RefillSpeed;
import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.ValidDurationChronoUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * Configures the rate of data which should be transferred.
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

	@PositiveOrZero
	private Long initialCapacity;
	
	@NotNull
	private RefillSpeed refillSpeed = RefillSpeed.GREEDY;
	
}
