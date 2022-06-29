package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds all the relevant starter properties which can be configured with
 * Spring Boots application.properties / application.yml configuration files. 
 */
@ConfigurationProperties(prefix = Bucket4JBootProperties.PROPERTY_PREFIX)
public class Bucket4JBootProperties {

	public static final String PROPERTY_PREFIX = "bucket4j";
	
	/**
	 * Enables or disables the Bucket4j Spring Boot Starter.
	 */
	private Boolean enabled = true;
	
	/**
	 * Sets the cache implementation which should be auto configured.
	 * This property can be used if multiple caches are configured by the starter 
	 * and you have to choose one due to a startup error.
	 * <ul>
	 * 	<li>jcache</li>
	 *  <li>hazelcast</li>
	 *  <li>ignite</li>
	 *  <li>redis</li>
	 * </ul>
	 */
	private String cacheToUse;
	
	private List<Bucket4JConfiguration> filters = new ArrayList<>();
	
	/**
	 * A list of default metric tags which should be applied to all filters
	 */
	private List<MetricTag> defaultMetricTags = new ArrayList<>();
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public static String getPropertyPrefix() {
		return PROPERTY_PREFIX;
	}

	public List<Bucket4JConfiguration> getFilters() {
		return filters;
	}

	public void setFilters(List<Bucket4JConfiguration> filters) {
		this.filters = filters;
	}

	public List<MetricTag> getDefaultMetricTags() {
		return defaultMetricTags;
	}

	public void setDefaultMetricTags(List<MetricTag> defaultMetricTags) {
		this.defaultMetricTags = defaultMetricTags;
	}

	public String getCacheToUse() {
		return cacheToUse;
	}

	public void setCacheToUse(String cacheToUse) {
		this.cacheToUse = cacheToUse;
	}

}
