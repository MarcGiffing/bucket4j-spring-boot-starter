package com.giffing.bucket4j.spring.boot.starter.context.properties;

public class MetricTag {

	private String key;
	
	private String expression;

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
	
}
