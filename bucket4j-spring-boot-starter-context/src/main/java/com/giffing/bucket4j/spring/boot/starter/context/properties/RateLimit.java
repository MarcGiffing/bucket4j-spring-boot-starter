package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.FilterKeyType;

public class RateLimit {
	
	private FilterKeyType filterKeyType = FilterKeyType.DEFAULT;
	
	/**
	 * SpEl condition to check if the rate limit should be executed. If null there is no check. 
	 */
	private String executeCondition;
	
	/**
	 * SpEl condition to check if the rate-limit should apply. If null there is no check.
	 */
	private String skipCondition;
	
	private String expression;

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
	
	public FilterKeyType getFilterKeyType() {
		return filterKeyType;
	}

	public void setFilterKeyType(FilterKeyType filterKeyType) {
		this.filterKeyType = filterKeyType;
	}

	public String getExecuteCondition() {
		return executeCondition;
	}

	public void setExecuteCondition(String executeCondition) {
		this.executeCondition = executeCondition;
	}
	
}
