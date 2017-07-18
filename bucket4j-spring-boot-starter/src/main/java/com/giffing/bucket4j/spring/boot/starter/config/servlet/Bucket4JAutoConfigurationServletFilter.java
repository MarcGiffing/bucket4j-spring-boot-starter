package com.giffing.bucket4j.spring.boot.starter.config.servlet;

import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.servlet.Filter;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.servlet.ServletRequestFilter;

/**
 * Configures Servlet {@link Filter}s for Bucket4Js rate limit.
 * 
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnClass({ Caching.class, JCacheCacheManager.class })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@ConditionalOnMissingBean(value = CacheManager.class, name = "cacheResolver")
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
public class Bucket4JAutoConfigurationServletFilter extends Bucket4JBaseConfiguration {

	@Autowired
	private Bucket4JBootProperties properties;
	
	@Autowired
	private org.springframework.cache.CacheManager cacheManager;
	
	@Autowired
	private BeanFactory beanFactory;

	
	@Bean
	public ExpressionParser servletFilterExpressionParser() {
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader());
		ExpressionParser parser = new SpelExpressionParser(config);
		
		return parser;
	}
	
	@Bean
	@Conditional(ConfigCondition1.class)
	public FilterRegistrationBean bucket4JFilter1() {
		return getFilterRegistrationBean(0);
	}
	
	public static class ConfigCondition1 extends AllNestedConditions {
		public ConfigCondition1() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[0].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[0].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	} 
	
	
	@Bean
	@Conditional(ConfigCondition2.class)
	public FilterRegistrationBean bucket4JFilter2() {
		return getFilterRegistrationBean(1);
	}
	
	public static class ConfigCondition2 extends AllNestedConditions {
		public ConfigCondition2() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[1].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[1].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	
	
	@Bean
	@Conditional(ConfigCondition3.class)
	public FilterRegistrationBean bucket4JFilter3() {
		return getFilterRegistrationBean(2);
	}
	
	public static class ConfigCondition3 extends AllNestedConditions {
		public ConfigCondition3() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[2].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[2].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition4.class)
	public FilterRegistrationBean bucket4JFilter4() {
		return getFilterRegistrationBean(3);
	}
	
	public static class ConfigCondition4 extends AllNestedConditions {
		public ConfigCondition4() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[3].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[3].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition5.class)
	public FilterRegistrationBean bucket4JFilter5() {
		return getFilterRegistrationBean(4);
	}
	
	public static class ConfigCondition5 extends AllNestedConditions {
		public ConfigCondition5() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[4].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[4].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition6.class)
	public FilterRegistrationBean bucket4JFilter6() {
		return getFilterRegistrationBean(5);
	}
	
	public static class ConfigCondition6 extends AllNestedConditions {
		public ConfigCondition6() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[5].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[5].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition7.class)
	public FilterRegistrationBean bucket4JFilter7() {
		return getFilterRegistrationBean(6);
	}
	
	public static class ConfigCondition7 extends AllNestedConditions {
		public ConfigCondition7() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[6].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[6].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition8.class)
	public FilterRegistrationBean bucket4JFilter8() {
		return getFilterRegistrationBean(7);
	}
	
	public static class ConfigCondition8 extends AllNestedConditions {
		public ConfigCondition8() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[7].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[7].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition9.class)
	public FilterRegistrationBean bucket4JFilter9() {
		return getFilterRegistrationBean(8);
	}
	
	public static class ConfigCondition9 extends AllNestedConditions {
		public ConfigCondition9() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[8].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[8].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition10.class)
	public FilterRegistrationBean bucket4JFilter10() {
		return getFilterRegistrationBean(9);
	}
	
	public static class ConfigCondition10 extends AllNestedConditions {
		public ConfigCondition10() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[9].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[9].filter-method", havingValue = "servlet", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	private FilterRegistrationBean getFilterRegistrationBean(int position) {
		Integer filterCount = 0;
		if(properties.getFilters().size() >= (position+1)) {
			Bucket4JConfiguration filter = properties.getFilters().get(position);
			filterCount++;
			
			FilterConfiguration filterConfig = buildFilterConfig(filter, cacheManager, servletFilterExpressionParser(), beanFactory);
			
			
			FilterRegistrationBean registration = new FilterRegistrationBean();
			registration.setName("bucket4JRequestFilter" + position);
	        registration.setFilter(new ServletRequestFilter(filterConfig));
	        registration.addUrlPatterns(filter.getUrl());
	        registration.setOrder(filter.getFilterOrder());
	        
	        return registration;
		}
		
		return null;
	}

	
	

}
