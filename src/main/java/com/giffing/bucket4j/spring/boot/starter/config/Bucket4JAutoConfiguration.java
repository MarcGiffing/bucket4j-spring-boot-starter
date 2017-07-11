package com.giffing.bucket4j.spring.boot.starter.config;

import java.time.Duration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.Bucket4JFilterConfig;
import com.giffing.bucket4j.spring.boot.starter.filter.Bucket4JRequestFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = "enabled", matchIfMissing = true)
@ConditionalOnClass({ Caching.class, JCacheCacheManager.class })
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@ConditionalOnMissingBean(value = CacheManager.class, name = "cacheResolver")
public class Bucket4JAutoConfiguration     {

	@Autowired
	private Bucket4JBootProperties properties;
	
	@Autowired
	private org.springframework.cache.CacheManager cacheManager;

	
	@SuppressWarnings("unchecked")
	public Cache<String, GridBucketState> getJCache(String cacheName) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if(springCache == null) {
        	throw new IllegalStateException("Please provide a cache with the name " + cacheName);
        }
		
        return (Cache<String, GridBucketState>) springCache.getNativeCache();
    }
	

	public Bucket4JBootProperties getProperties() {
		return properties;
	}

	public void setProperties(Bucket4JBootProperties properties) {
		this.properties = properties;
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[0].url")
	public FilterRegistrationBean bucket4JFilter1() {
		return getFilterRegistrationBean(0);
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[1].url")
	public FilterRegistrationBean bucket4JFilter2() {
		return getFilterRegistrationBean(1);
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[2].url")
	public FilterRegistrationBean bucket4JFilter3() {
		return getFilterRegistrationBean(2);
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[3].url")
	public FilterRegistrationBean bucket4JFilter4() {
		return getFilterRegistrationBean(3);
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[4].url")
	public FilterRegistrationBean bucket4JFilter5() {
		return getFilterRegistrationBean(4);
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[5].url")
	public FilterRegistrationBean bucket4JFilter6() {
		return getFilterRegistrationBean(5);
	}
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[6].url")
	public FilterRegistrationBean bucket4JFilter7() {
		return getFilterRegistrationBean(6);
	}
	
	

	private FilterRegistrationBean getFilterRegistrationBean(int position) {
		Integer filterCount = 0;
		if(properties.getConfigs().size() >= (position+1)) {
			Bucket4JConfiguration config = properties.getConfigs().get(position);
			filterCount++;
			
			ProxyManager<String> buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(getJCache(config.getCacheName()));
			
			ConfigurationBuilder<?> configBuilder = Bucket4j.configurationBuilder();
			for (Bucket4JBandWidth bandWidth : config.getBandwidths()) {
				configBuilder = configBuilder.addLimit(Bandwidth.simple(bandWidth.getCapacity(), Duration.of(bandWidth.getTime(), bandWidth.getUnit())));
			};
			
			Bucket4JFilterConfig filterConfig = new Bucket4JFilterConfig();
			filterConfig.setBuckets(buckets);
			filterConfig.setConfig(configBuilder.buildConfiguration());
			switch(config.getFilterType()) {
			case IP:
				filterConfig.setKeyFilter( (request) -> request.getRemoteAddr());
				break;
			default:
				filterConfig.setKeyFilter( (request) -> "1");
				break;
			}
			
			FilterRegistrationBean registration = new FilterRegistrationBean();
			registration.setName("bucket4JRequestFilter" + position);
	        registration.setFilter(new Bucket4JRequestFilter(filterConfig));
	        registration.addUrlPatterns(config.getUrl());
	        
	        return registration;
		}
		
		return null;
	}




//	@Bean
//	public List<FilterRegistrationBean> xyasu() {
//		List<FilterRegistrationBean> result = new ArrayList<>();
//		Integer filterCount = 0;
//		for ( Bucket4JConfiguration config : properties.getConfigs() ) {
//			filterCount++;
//			
//			ProxyManager<String> buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(getJCache(config.getCacheName()));
//			
//			ConfigurationBuilder<?> configBuilder = Bucket4j.configurationBuilder();
//			for (Bucket4JBandWidth bandWidth : config.getBandwidths()) {
//				configBuilder = configBuilder.addLimit(Bandwidth.simple(bandWidth.getCapacity(), Duration.of(bandWidth.getTime(), bandWidth.getUnit())));
//			};
//			
//			Bucket4JFilterConfig filterConfig = new Bucket4JFilterConfig();
//			filterConfig.setBuckets(buckets);
//			filterConfig.setConfig(configBuilder.buildConfiguration());
//			switch(config.getFilterType()) {
//			case IP:
//				filterConfig.setKeyFilter( (request) -> request.getRemoteAddr());
//				break;
//			default:
//				filterConfig.setKeyFilter( (request) -> "1");
//				break;
//			}
//			
//			FilterRegistrationBean registration = new FilterRegistrationBean();
//	        registration.setFilter(new Bucket4JRequestFilter(filterConfig));
//	        registration.addUrlPatterns(config.getUrl());
//	        
//	        result.add(registration);
//	        
//		}
//		
//		return result;
//		
//	}

	
}
