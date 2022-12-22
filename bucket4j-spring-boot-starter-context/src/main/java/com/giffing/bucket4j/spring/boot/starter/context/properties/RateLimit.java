package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.List;

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
	private String expression = "1";

	/**
	 * The number of tokens that should be consumed 
	 */
	private Integer numTokens = 1;

	private List<BandWidth> bandwidths = new ArrayList<>();

}