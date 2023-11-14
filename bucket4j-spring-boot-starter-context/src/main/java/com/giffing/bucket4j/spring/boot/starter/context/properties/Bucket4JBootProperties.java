package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import lombok.Data;

/**
 * Holds all the relevant starter properties which can be configured with
 * Spring Boots application.properties / application.yml configuration files. 
 */
@Data
@ConfigurationProperties(prefix = Bucket4JBootProperties.PROPERTY_PREFIX)
@Validated
public class Bucket4JBootProperties {

	public static final String PROPERTY_PREFIX = "bucket4j";
	
	/**
	 * Enables or disables the Bucket4j Spring Boot Starter.
	 */
	@NotNull
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

	private boolean filterConfigCachingEnabled = false;

	/**
	 * If Filter configuration caching is enabled, a cache with this name should exist, or it will cause an exception.
	 */
	@NotBlank
	private String filterConfigCacheName = "filterConfigCache";

	private List<Bucket4JConfiguration> filters = new ArrayList<>();

	public void setFilters(List<Bucket4JConfiguration> filters){
		//validate that all filters have an id if filter caching is enabled
		if(filterConfigCachingEnabled && filters.stream().anyMatch(filter -> filter.getId() == null)) {
			throw new IllegalArgumentException("FilterConfiguration caching is enabled, but not all filters have an identifier configured");
		}
		this.filters = filters;
	}

	/**
	 * A list of default metric tags which should be applied to all filters
	 */
	private List<MetricTag> defaultMetricTags = new ArrayList<>();

	public static String getPropertyPrefix() {
		return PROPERTY_PREFIX;
	}

}
