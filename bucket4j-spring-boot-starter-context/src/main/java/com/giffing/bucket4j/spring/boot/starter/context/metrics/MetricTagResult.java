package com.giffing.bucket4j.spring.boot.starter.context.metrics;

import java.util.List;

import lombok.Data;

@Data
public class MetricTagResult {

	private String key;
	
	private String value;
	
	private List<MetricType> types;

	public MetricTagResult(String key, String value, List<MetricType> types) {
		this.key = key;
		this.value = value;
		this.setTypes(types);
	}

}
