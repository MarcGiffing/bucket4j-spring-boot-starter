package com.giffing.bucket4j.spring.boot.starter.config.zuul;

import java.time.Duration;

import javax.cache.Caching;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBandWidth;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.zuul.ZuulRateLimitFilter;
import com.netflix.zuul.ZuulFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;

@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = "enabled", matchIfMissing = true)
@ConditionalOnClass({ Caching.class, JCacheCacheManager.class, ZuulFilter.class })
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@ConditionalOnMissingBean(value = CacheManager.class, name = "cacheResolver")
public class Bucket4JAutoConfigurationZuul extends Bucket4JBaseConfiguration {

	@Autowired
	private Bucket4JBootProperties properties;
	
	@Autowired
	private org.springframework.cache.CacheManager cacheManager;
	
	@Autowired
	private BeanFactory beanFactory;

	@Bean
	public ExpressionParser zuulExpressionParser() {
		ExpressionParser parser = new SpelExpressionParser();
		return parser;
	}
	
	
	@Bean
	@ConditionalOnProperty(value= Bucket4JBootProperties.PROPERTY_PREFIX + ".configs[0].url")
	public ZuulFilter zuulFilter1() {
		return createZuulFilter(0);
	}
	
	private ZuulFilter createZuulFilter(int position) {
		Integer filterCount = 0;
		if(properties.getConfigs().size() >= (position+1)) {
			Bucket4JConfiguration config = properties.getConfigs().get(position);
			filterCount++;
			
			ProxyManager<String> buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(jCache(config.getCacheName(), cacheManager));
			
			ConfigurationBuilder<?> configBuilder = Bucket4j.configurationBuilder();
			for (Bucket4JBandWidth bandWidth : config.getBandwidths()) {
				configBuilder = configBuilder.addLimit(Bandwidth.simple(bandWidth.getCapacity(), Duration.of(bandWidth.getTime(), bandWidth.getUnit())));
			};
			
			FilterConfiguration filterConfig = new FilterConfiguration();
			filterConfig.setBuckets(buckets);
			filterConfig.setConfig(configBuilder.buildConfiguration());
			filterConfig.setKeyFilter(getKeyFilter(config, zuulExpressionParser(), beanFactory));
			
			
	        return new ZuulRateLimitFilter(filterConfig);
		}
		
		return null;
	}

	
}
