package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.ArrayList;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.RateLimitCheck;

public class FilterConfiguration {

	private List<RateLimitCheck> rateLimitChecks = new ArrayList<>();
	
	private String url;
	
	private int order;


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public List<RateLimitCheck> getRateLimitChecks() {
		return rateLimitChecks;
	}

	public void setRateLimitChecks(List<RateLimitCheck> rateLimitChecks) {
		this.rateLimitChecks = rateLimitChecks;
	}

}
