package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.ArrayList;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

public class Bucket4jConfigurationHolder {

	private List<Bucket4JConfiguration> filterConfiguration = new ArrayList<>();

	public void addFilterConfiguration(Bucket4JConfiguration filterConfiguration) {
		getFilterConfiguration().add(filterConfiguration);
	}
	
	public List<Bucket4JConfiguration> getFilterConfiguration() {
		return filterConfiguration;
	}

	public void setFilterConfiguration(List<Bucket4JConfiguration> filterConfiguration) {
		this.filterConfiguration = filterConfiguration;
	}
	
}
