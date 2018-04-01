package com.giffing.bucket4j.spring.boot.starter.config;

import java.time.Duration;

import javax.cache.Cache;
import javax.cache.CacheManager;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.config.zuul.Bucket4JAutoConfigurationZuul;
import com.giffing.bucket4j.spring.boot.starter.context.BandWidthConfig;
import com.giffing.bucket4j.spring.boot.starter.context.Condition;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.KeyFilter;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;
import com.giffing.bucket4j.spring.boot.starter.exception.MissingKeyFilterExpressionException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.grid.GridBucketState;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCache;

/**
 * Holds helper Methods which are reused by the {@link Bucket4JAutoConfigurationServletFilter} and 
 * the {@link Bucket4JAutoConfigurationZuul} configuration classes
 */
public abstract class Bucket4JBaseConfiguration {
	
	/**
	 * This methods 
	 * 
	 * @param cacheName the name of the cache to retrieve
	 * @param cacheManager
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Cache<String, GridBucketState> jCache(String cacheName, CacheManager cacheManager) {
        Cache springCache = cacheManager.getCache(cacheName);
        if(springCache == null) {
        	throw new JCacheNotFoundException(cacheName);
        }
		
        return (Cache<String, GridBucketState>) springCache;
    }
	
	/**
	 * This method  
	 * 
	 * @param config
	 * @param cacheManager
	 * @param expressionParser
	 * @param beanFactory
	 * @return
	 */
	public FilterConfiguration buildFilterConfig(Bucket4JConfiguration config, CacheManager cacheManager, ExpressionParser expressionParser, BeanFactory beanFactory) {
		
		FilterConfiguration filterConfig = new FilterConfiguration();
		filterConfig.setUrl(config.getUrl());
		filterConfig.setOrder(config.getFilterOrder());
		filterConfig.setStrategy(config.getStrategy());
		filterConfig.setHttpResponseBody(config.getHttpResponseBody());
		ProxyManager<String> buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(jCache(config.getCacheName(), cacheManager));
		
		config.getRateLimits().forEach(rl -> {
			ConfigurationBuilder<?> configBuilder = Bucket4j.configurationBuilder();
			for (BandWidthConfig bandWidth : rl.getBandwidths()) {
				configBuilder = configBuilder.addLimit(Bandwidth.simple(bandWidth.getCapacity(), Duration.of(bandWidth.getTime(), bandWidth.getUnit())));
			};
			
			final ConfigurationBuilder<?> configBuilderToUse = configBuilder;
			RateLimitCheck rlc = (servletRequest) -> {
				
		        boolean skipRateLimit = false;
		        if (rl.getSkipCondition() != null) {
		        	skipRateLimit = skipCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
		        } 
		        
		        if(rl.getExecuteCondition() != null && !skipRateLimit) {
		        	skipRateLimit = !executeCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
		        }
		        
		        if(!skipRateLimit) {
		        	String key = getKeyFilter(filterConfig.getUrl(), rl, expressionParser, beanFactory).key(servletRequest);
		        	Bucket bucket = buckets.getProxy(key, () -> configBuilderToUse.buildConfiguration());
		        	
		        	ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
		        	
		        	return probe;
		        }
				return null;
				
			};
			filterConfig.getRateLimitChecks().add(rlc);
			
		});
		
		return filterConfig;
	}
	
	/**
	 * Creates the key filter lambda which is responsible to decide how the rate limit will be performed. The key
	 * is the unique identifier like an IP address or a username.
	 * 
	 * @param url is used to generated a unique cache key
	 * @param rateLimit the {@link RateLimit} configuration which holds the skip condition string
	 * @param expressionParser is used to evaluate the expression if the filter key type is EXPRESSION.
	 * @param beanFactory used to get full access to all java beans in the SpEl
	 * @return should not been null. If no filter key type is matching a plain 1 is returned so that all requests uses the same key.
	 */
	public KeyFilter getKeyFilter(String url, RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		
		switch(rateLimit.getFilterKeyType()) {
		case IP:
			return (request) -> url + "-" + request.getRemoteAddr();
		case EXPRESSION:
			String expression = rateLimit.getExpression();
			if(StringUtils.isEmpty(expression)) {
				throw new MissingKeyFilterExpressionException();
			}
			StandardEvaluationContext context = new StandardEvaluationContext();
			context.setBeanResolver(new BeanFactoryResolver(beanFactory));
			return  (request) -> {
				//TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
				Expression expr = expressionParser.parseExpression(rateLimit.getExpression()); 
				final String value = expr.getValue(context, request, String.class);
				return url + "-" + value;
			};
		
		}
		return (request) -> url + "-" + "1";
	}
	
	/**
	 * Creates the lambda for the skip condition which will be evaluated on each request
	 * 
	 * @param rateLimit the {@link RateLimit} configuration which holds the skip condition string
	 * @param expressionParser is used to evaluate the skip expression
	 * @param beanFactory used to get full access to all java beans in the SpEl
	 * @return the lamdba condition which will be evaluated lazy - null if there is no condition available.
	 */
	public Condition skipCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		
		if(rateLimit.getSkipCondition() != null) {
			return  (request) -> {
				Expression expr = expressionParser.parseExpression(rateLimit.getSkipCondition()); 
				Boolean value = expr.getValue(context, request, Boolean.class);
				return value;
			};
		}
		return null;
	}
	
	/**
	 * Creates the lambda for the execute condition which will be evaluated on each request.
	 * 
	 * @param rateLimit the {@link RateLimit} configuration which holds the execute condition string
	 * @param expressionParser is used to evaluate the execution expression
	 * @param beanFactory used to get full access to all java beans in the SpEl
	 * @return the lamdba condition which will be evaluated lazy - null if there is no condition available.
	 */
	public Condition executeCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		
		if(rateLimit.getExecuteCondition() != null) {
			return (request) -> {
				Expression expr = expressionParser.parseExpression(rateLimit.getExecuteCondition()); 
				Boolean value = expr.getValue(context, request, Boolean.class);
				return value;
			};
		}
		return null;
	}
}
