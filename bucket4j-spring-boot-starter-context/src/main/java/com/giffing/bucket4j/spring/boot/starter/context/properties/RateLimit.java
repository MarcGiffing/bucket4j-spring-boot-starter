package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.List;

public class RateLimit {
	
	/**
	 * SpEl condition to check if the rate limit should be executed. If null there is no check. 
	 */
	private String executeCondition;
	
	/**
	 * SpEl condition to check if the rate-limit should apply. If null there is no check.
	 */
	private String skipCondition;

	private String expression = "1";

	private Integer cost = 1;

	public Integer getCost() {
		return cost;
	}

	public void setCost(Integer cost) {
		this.cost = cost;
	}

	private List<BandWidth> bandwidths = new ArrayList<>();
	
	/**
	 * SPEL expression to dynamic evaluate filter key 
	 */
	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String getSkipCondition() {
		return skipCondition;
	}

	public void setSkipCondition(String skipCondition) {
		this.skipCondition = skipCondition;
	}
	
	public List<BandWidth> getBandwidths() {
		return bandwidths;
	}

	public void setBandwidths(List<BandWidth> bandwidths) {
		this.bandwidths = bandwidths;
	}

	public String getExecuteCondition() {
		return executeCondition;
	}

	public void setExecuteCondition(String executeCondition) {
		this.executeCondition = executeCondition;
	}

}
