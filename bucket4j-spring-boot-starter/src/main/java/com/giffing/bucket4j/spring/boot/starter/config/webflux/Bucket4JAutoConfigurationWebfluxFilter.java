package com.giffing.bucket4j.spring.boot.starter.config.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.WebFilter;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBaseConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBoot1ActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.springboot.SpringBoot2ActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.webflux.WebfluxWebFilter;

import io.github.bucket4j.grid.ProxyManager;

/**
 * Configures up to 10 Servlet Filters for Bucket4Js rate limit.
 * 
 * Technical problem: The dynamic creation of the Webflux WebFilters failed cause when
 * registering them manually in the application context the Beans arn't detected as Servlet Filters
 * and therefore not configured correctly. The current workaround is the define 10 different methods
 * which creates individual Webfilters conditional on properties.
 * 
 */
@Configuration
@ConditionalOnClass({ WebFilter.class  })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = AsyncCacheResolver.class)
@EnableConfigurationProperties({ Bucket4JBootProperties.class })
@Import(value = {SpringBoot1ActuatorConfig.class, SpringBoot2ActuatorConfig.class })
public class Bucket4JAutoConfigurationWebfluxFilter extends Bucket4JBaseConfiguration<ServerHttpRequest> {

	private Logger log = LoggerFactory.getLogger(Bucket4JAutoConfigurationWebfluxFilter.class);
	
	@Autowired
	private Bucket4JBootProperties properties;
	
	@Autowired
	private ConfigurableBeanFactory beanFactory;
	
	@Autowired
	private AsyncCacheResolver cacheResolver;

	@Bean
	@Qualifier("WEBFLUX")
	public Bucket4jConfigurationHolder servletConfigurationHolder() {
		return new Bucket4jConfigurationHolder();
	}
	
	@Bean
	public ExpressionParser webFilterExpressionParser() {
		SpelParserConfiguration config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, this.getClass().getClassLoader());
		ExpressionParser parser = new SpelExpressionParser(config);
		
		return parser;
	}
	
	@Bean
	@Conditional(ConfigCondition1.class)
	public WebFilter bucket4JFilter1() {
		return getWebFilter(0);
	}
	
	public static class ConfigCondition1 extends AllNestedConditions {
		public ConfigCondition1() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[0].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[0].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	} 
	
	
	@Bean
	@Conditional(ConfigCondition2.class)
	public WebFilter bucket4JFilter2() {
		return getWebFilter(1);
	}
	
	public static class ConfigCondition2 extends AllNestedConditions {
		public ConfigCondition2() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[1].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[1].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	
	
	@Bean
	@Conditional(ConfigCondition3.class)
	public WebFilter bucket4JFilter3() {
		return getWebFilter(2);
	}
	
	public static class ConfigCondition3 extends AllNestedConditions {
		public ConfigCondition3() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[2].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[2].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition4.class)
	public WebFilter bucket4JFilter4() {
		return getWebFilter(3);
	}
	
	public static class ConfigCondition4 extends AllNestedConditions {
		public ConfigCondition4() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[3].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[3].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition5.class)
	public WebFilter bucket4JFilter5() {
		return getWebFilter(4);
	}
	
	public static class ConfigCondition5 extends AllNestedConditions {
		public ConfigCondition5() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[4].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[4].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition6.class)
	public WebFilter bucket4JFilter6() {
		return getWebFilter(5);
	}
	
	public static class ConfigCondition6 extends AllNestedConditions {
		public ConfigCondition6() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[5].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[5].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition7.class)
	public WebFilter bucket4JFilter7() {
		return getWebFilter(6);
	}
	
	public static class ConfigCondition7 extends AllNestedConditions {
		public ConfigCondition7() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[6].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[6].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition8.class)
	public WebFilter bucket4JFilter8() {
		return getWebFilter(7);
	}
	
	public static class ConfigCondition8 extends AllNestedConditions {
		public ConfigCondition8() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[7].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[7].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition9.class)
	public WebFilter bucket4JFilter9() {
		return getWebFilter(8);
	}
	
	public static class ConfigCondition9 extends AllNestedConditions {
		public ConfigCondition9() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[8].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[8].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition10.class)
	public WebFilter bucket4JFilter10() {
		return getWebFilter(9);
	}
	
	public static class ConfigCondition10 extends AllNestedConditions {
		public ConfigCondition10() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[9].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[9].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition11.class)
	public WebFilter bucket4JFilter11() {
		return getWebFilter(10);
	}
	
	public static class ConfigCondition11 extends AllNestedConditions {
		public ConfigCondition11() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[10].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[10].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition12.class)
	public WebFilter bucket4JFilter12() {
		return getWebFilter(11);
	}
	
	public static class ConfigCondition12 extends AllNestedConditions {
		public ConfigCondition12() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[11].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[11].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition13.class)
	public WebFilter bucket4JFilter13() {
		return getWebFilter(12);
	}
	
	public static class ConfigCondition13 extends AllNestedConditions {
		public ConfigCondition13() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[12].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[12].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition14.class)
	public WebFilter bucket4JFilter14() {
		return getWebFilter(13);
	}
	
	public static class ConfigCondition14 extends AllNestedConditions {
		public ConfigCondition14() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[13].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[13].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition15.class)
	public WebFilter bucket4JFilter15() {
		return getWebFilter(14);
	}
	
	public static class ConfigCondition15 extends AllNestedConditions {
		public ConfigCondition15() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[14].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[14].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition16.class)
	public WebFilter bucket4JFilter16() {
		return getWebFilter(15);
	}
	
	public static class ConfigCondition16 extends AllNestedConditions {
		public ConfigCondition16() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[15].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[15].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition17.class)
	public WebFilter bucket4JFilter17() {
		return getWebFilter(16);
	}
	
	public static class ConfigCondition17 extends AllNestedConditions {
		public ConfigCondition17() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[16].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[16].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition18.class)
	public WebFilter bucket4JFilter18() {
		return getWebFilter(17);
	}
	
	public static class ConfigCondition18 extends AllNestedConditions {
		public ConfigCondition18() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[17].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[17].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition19.class)
	public WebFilter bucket4JFilter19() {
		return getWebFilter(18);
	}
	
	public static class ConfigCondition19 extends AllNestedConditions {
		public ConfigCondition19() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[18].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[18].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	@Bean
	@Conditional(ConfigCondition20.class)
	public WebFilter bucket4JFilter20() {
		return getWebFilter(19);
	}
	
	public static class ConfigCondition20 extends AllNestedConditions {
		public ConfigCondition20() { super(ConfigurationPhase.REGISTER_BEAN); }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[19].url")
		static class OnEnabled { }
		
		@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value =  "filters[19].filter-method", havingValue = "webflux", matchIfMissing = true)
		static class OnServletFilter{ }
	}
	
	private WebFilter getWebFilter(int position) {
		Integer filterCount = 0;
		if(properties.getFilters().size() >= (position+1)) {
			Bucket4JConfiguration filter = properties.getFilters().get(position);
			filterCount++;
			
			FilterConfiguration<ServerHttpRequest> filterConfig = buildFilterConfig(filter, createProxyManager(filter), webFilterExpressionParser(), beanFactory);
			
			servletConfigurationHolder().addFilterConfiguration(filter);
			
			WebFilter registration = new WebfluxWebFilter(filterConfig);
	        
	        log.info("create-webflux-filter;{};{};{}", position, filter.getCacheName(), filter.getUrl());
	        return registration;
		}
		
		return null;
	}

	@Override
	protected ProxyManager<String> createProxyManager(Bucket4JConfiguration config) {
		return cacheResolver.resolve(config.getCacheName());
	}


}
