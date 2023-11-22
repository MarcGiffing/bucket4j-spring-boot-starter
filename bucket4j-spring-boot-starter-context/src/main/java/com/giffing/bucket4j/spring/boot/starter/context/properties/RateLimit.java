package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import io.github.bucket4j.TokensInheritanceStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
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
	private List<BandWidth> bandwidths = new ArrayList<>();

	@JsonIgnore
	@AssertTrue(message = "Rate limits cannot contain bandwidths with identical identifiers or null values when " +
			"using a different inheritance strategy than 'RESET'.")
	public boolean isBandwidthIdsValid(){
		Set<String> idSet = new HashSet<>();
		for (BandWidth bandWidth : bandwidths) {
			String id = bandWidth.getId();
			if(id == null && tokensInheritanceStrategy == TokensInheritanceStrategy.RESET) continue;
			if (!idSet.add(bandWidth.getId())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The token inheritance strategy to use when replacing the configuration of a bucket
	 */
	@NotNull
	private TokensInheritanceStrategy tokensInheritanceStrategy = TokensInheritanceStrategy.RESET;
}