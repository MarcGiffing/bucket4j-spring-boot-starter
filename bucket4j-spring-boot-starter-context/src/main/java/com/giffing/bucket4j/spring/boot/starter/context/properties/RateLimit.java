package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import lombok.Data;

@Data
public class RateLimit {
	
	/**
	 * SpEl condition to check if the rate limit should be executed. If null there is no check. 
	 */
	private String executeCondition;
	
	/**
	 * SpEl condition to check if the rate-limit should apply. If null there is no check.
	 */
	private String skipCondition;

	/**
	 * SPEL expression to dynamic evaluate filter key 
	 */
	@NotBlank
	private String cacheKey = "1";
	
	@Null(message = "The expression is depcreated since 0.8. Please use cache-key instead")
	@Deprecated
	private String expression;

	/**
	 * The number of tokens that should be consumed 
	 */
	@NotNull
	@Min(1)
	private Integer numTokens = 1;

	@NotEmpty
	private List<BandWidth> bandwidths = new ArrayList<>();

}