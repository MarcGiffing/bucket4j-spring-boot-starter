package com.giffing.bucket4j.spring.boot.starter.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.giffing.bucket4j.spring.boot.starter.filter.Bucket4JFilterType;

@ConfigurationProperties(prefix = Bucket4JBootProperties.PROPERTY_PREFIX)
public class Bucket4JBootProperties {

	public static final String PROPERTY_PREFIX = "bucket4j.rate-limit";
	
	private Boolean enabled = true;
	
	private List<Bucket4JConfiguration> configs = new ArrayList<>();
	
	
	public static class Bucket4JConfiguration {

		private Bucket4JFilterType filterType = Bucket4JFilterType.DEFAULT;
		
		private String cacheName = "buckets";
		
		private String url = "/*";
		
		private int filterOrder = Integer.MIN_VALUE + 1;
		
		private List<Bucket4JBandWidth> bandwidths = new ArrayList<>();
		
		public Bucket4JConfiguration() {
			
		}
		
		public String getCacheName() {
			return cacheName;
		}

		public void setCacheName(String cacheName) {
			this.cacheName = cacheName;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public List<Bucket4JBandWidth> getBandwidths() {
			return bandwidths;
		}

		public void setBandwidths(List<Bucket4JBandWidth> bandwidths) {
			this.bandwidths = bandwidths;
		}

		public Bucket4JFilterType getFilterType() {
			return filterType;
		}

		public void setFilterType(Bucket4JFilterType filterType) {
			this.filterType = filterType;
		}

		public int getFilterOrder() {
			return filterOrder;
		}

		public void setFilterOrder(int filterOrder) {
			this.filterOrder = filterOrder;
		}
		
	}
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public static String getPropertyPrefix() {
		return PROPERTY_PREFIX;
	}

	public List<Bucket4JConfiguration> getConfigs() {
		return configs;
	}

	public void setConfigs(List<Bucket4JConfiguration> configs) {
		this.configs = configs;
	}
	
}
