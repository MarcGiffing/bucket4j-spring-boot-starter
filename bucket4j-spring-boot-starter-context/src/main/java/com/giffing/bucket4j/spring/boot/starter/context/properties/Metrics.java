package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

public class Metrics {

	private boolean enabled = true;
	
	private List<MetricType> types = Arrays.asList(MetricType.values());
	
	private List<MetricTag> tags = new ArrayList<>();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public List<MetricTag> getTags() {
		return tags;
	}

	public void setTags(List<MetricTag> tags) {
		this.tags = tags;
	}

	public List<MetricType> getTypes() {
		return types;
	}

	public void setTypes(List<MetricType> types) {
		this.types = types;
	}
	
}
