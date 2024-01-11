package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.ValidBandWidthIds;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import io.github.bucket4j.TokensInheritanceStrategy;
import lombok.Data;

@Data
@ValidBandWidthIds
public class RateLimit implements Serializable {
	
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
	
	@Valid
	private List<ExecutePredicateDefinition> skipPredicates = new ArrayList<>();

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
	@Valid
	private List<BandWidth> bandwidths = new ArrayList<>();

	/**
	 * The token inheritance strategy to use when replacing the configuration of a bucket
	 */
	@NotNull
	private TokensInheritanceStrategy tokensInheritanceStrategy = TokensInheritanceStrategy.RESET;
}