package com.giffing.bucket4j.spring.boot.starter.context.metrics;

public class MetricTagResult {

	private String key;
	
	private String value;

	public MetricTagResult(String key, String value) {
		this.key = key;
		this.value = value;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
