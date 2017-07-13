package com.giffing.bucket4j.spring.boot.starter.config.servlet;

import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties;

public class EnabledAndServletFilterProperty  extends AllNestedConditions {

	public EnabledAndServletFilterProperty() {
		super(ConfigurationPhase.PARSE_CONFIGURATION);
	}
	
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
	static class OnEnabled {
	}
	
	@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"filter-method"}, havingValue = "servlet", matchIfMissing = true)
	static class OnServletFilter{
	}
	
}