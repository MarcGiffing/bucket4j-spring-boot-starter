package com.giffing.bucket4j.spring.boot.starter.config.zuul;

import java.time.Duration;

import javax.cache.Caching;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ConfigurationCondition.ConfigurationPhase;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4JBandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.zuul.ZuulRateLimitFilter;
import com.netflix.zuul.ZuulFilter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;

/**
 * Configures {@link ZuulFilter}s for Bucket4Js rate limit.
 * 
 */
@Configuration
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
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
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader());
		ExpressionParser parser = new SpelExpressionParser(config);
		return parser;
	}
	
	
	@Bean
	@Conditional(ConfigCondition1.class)
	public ZuulFilter zuulFilter1() {
		return createZuulFilter(0);
	}
	
	public static class ConfigCondition1 extends AllNestedConditions {
		public ConfigCondition1() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[0].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[0].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition2.class)
	public ZuulFilter zuulFilter2() {
		return createZuulFilter(1);
	}
	
	public static class ConfigCondition2 extends AllNestedConditions {
		public ConfigCondition2() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[1].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[1].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition3.class)
	public ZuulFilter zuulFilter3() {
		return createZuulFilter(2);
	}
	
	public static class ConfigCondition3 extends AllNestedConditions {
		public ConfigCondition3() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[2].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[2].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition4.class)
	public ZuulFilter zuulFilter4() {
		return createZuulFilter(3);
	}
	
	public static class ConfigCondition4 extends AllNestedConditions {
		public ConfigCondition4() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[3].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[3].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition5.class)
	public ZuulFilter zuulFilter5() {
		return createZuulFilter(4);
	}
	
	public static class ConfigCondition5 extends AllNestedConditions {
		public ConfigCondition5() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[4].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[4].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition6.class)
	public ZuulFilter zuulFilter6() {
		return createZuulFilter(5);
	}
	
	public static class ConfigCondition6 extends AllNestedConditions {
		public ConfigCondition6() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[5].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[5].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition7.class)
	public ZuulFilter zuulFilter7() {
		return createZuulFilter(6);
	}
	
	public static class ConfigCondition7 extends AllNestedConditions {
		public ConfigCondition7() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[6].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[6].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition8.class)
	public ZuulFilter zuulFilter8() {
		return createZuulFilter(7);
	}
	
	public static class ConfigCondition8 extends AllNestedConditions {
		public ConfigCondition8() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[7].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[7].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition9.class)
	public ZuulFilter zuulFilter9() {
		return createZuulFilter(8);
	}
	
	public static class ConfigCondition9 extends AllNestedConditions {
		public ConfigCondition9() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[8].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[8].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
	} 
	
	@Bean
	@Conditional(ConfigCondition10.class)
	public ZuulFilter zuulFilter10() {
		return createZuulFilter(9);
	}
	
	public static class ConfigCondition10 extends AllNestedConditions {
		public ConfigCondition10() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[9].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "configs[9].filter-method", havingValue = "zuul")
		static class OnServletFilter{ }
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
			filterConfig.setUrl(config.getUrl());
			filterConfig.setConfig(configBuilder.buildConfiguration());
			filterConfig.setKeyFilter(getKeyFilter(config, zuulExpressionParser(), beanFactory));
			filterConfig.setSkipCondition(filterCondition(config, zuulExpressionParser(), beanFactory));
			
	        return new ZuulRateLimitFilter(filterConfig);
		}
		
		return null;
	}

	
}
