package com.giffing.bucket4j.spring.boot.starter.context;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.ProxyManager;

public class FilterConfiguration {

	private KeyFilter keyFilter;
	
	private SkipCondition skipCondition;

	private ProxyManager<String> buckets;
	
	private BucketConfiguration config;
	
	private String url;
	
	private int order;

	public KeyFilter getKeyFilter() {
		return keyFilter;
	}

	public void setKeyFilter(KeyFilter keyFilter) {
		this.keyFilter = keyFilter;
	}

	public ProxyManager<String> getBuckets() {
		return buckets;
	}

	public void setBuckets(ProxyManager<String> buckets) {
		this.buckets = buckets;
	}

	public BucketConfiguration getConfig() {
		return config;
	}

	public void setConfig(BucketConfiguration config) {
		this.config = config;
	}

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

	public SkipCondition getSkipCondition() {
		return skipCondition;
	}

	public void setSkipCondition(SkipCondition condition) {
		this.skipCondition = condition;
	}

}
