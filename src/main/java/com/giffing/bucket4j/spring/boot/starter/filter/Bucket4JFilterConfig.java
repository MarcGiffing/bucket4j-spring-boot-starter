package com.giffing.bucket4j.spring.boot.starter.filter;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JKeyFilter;

import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.grid.ProxyManager;

public class Bucket4JFilterConfig {

	private Bucket4JKeyFilter keyFilter;
	
	private ProxyManager<String> buckets;
	
	private BucketConfiguration config;

	public Bucket4JKeyFilter getKeyFilter() {
		return keyFilter;
	}

	public void setKeyFilter(Bucket4JKeyFilter keyFilter) {
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
	
}
