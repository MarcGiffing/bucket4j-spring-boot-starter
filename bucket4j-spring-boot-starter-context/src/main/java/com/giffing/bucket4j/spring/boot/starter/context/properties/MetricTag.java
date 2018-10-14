package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.Arrays;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

public class MetricTag {

	private String key;
	
	private String expression;
	
	private List<MetricType> types = Arrays.asList(MetricType.values());

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public List<MetricType> getTypes() {
		return types;
	}

	public void setTypes(List<MetricType> types) {
		this.types = types;
	}
	
}
