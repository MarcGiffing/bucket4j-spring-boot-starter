package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;

import lombok.Data;

@Data
public class RateLimit {
	
	/**
	 * SpEl condition to check if the rate limit should be executed. If null there is no check. 
	 */
	private String executeCondition;
	
	@Valid
	private List<ExecutePredicateDefinition> executePredicates = new ArrayList<>();
	
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