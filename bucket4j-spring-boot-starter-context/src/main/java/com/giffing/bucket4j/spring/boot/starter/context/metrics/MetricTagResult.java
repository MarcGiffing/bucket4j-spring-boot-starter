package com.giffing.bucket4j.spring.boot.starter.context.metrics;

import java.util.List;

public class MetricTagResult {

	private String key;
	
	private String value;
	
	private List<MetricType> types;

	public MetricTagResult(String key, String value, List<MetricType> types) {
		this.key = key;
		this.value = value;
		this.setTypes(types);
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

	public List<MetricType> getTypes() {
		return types;
	}

	public void setTypes(List<MetricType> types) {
		this.types = types;
	}

}
