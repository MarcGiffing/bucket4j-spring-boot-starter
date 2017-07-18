package com.giffing.bucket4j.spring.boot.starter.config;

import java.time.Duration;

import javax.cache.Cache;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.config.zuul.Bucket4JAutoConfigurationZuul;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4JBandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.KeyFilter;
import com.giffing.bucket4j.spring.boot.starter.context.Condition;
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
	
	@SuppressWarnings("unchecked")
	public Cache<String, GridBucketState> jCache(String cacheName, CacheManager cacheManager) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if(springCache == null) {
        	throw new JCacheNotFoundException(cacheName);
        }
		
        return (Cache<String, GridBucketState>) springCache.getNativeCache();
    }
	
	public FilterConfiguration buildFilterConfig(Bucket4JConfiguration config, org.springframework.cache.CacheManager cacheManager, ExpressionParser expressionParser, BeanFactory beanFactory) {
		
		FilterConfiguration filterConfig = new FilterConfiguration();
		filterConfig.setUrl(config.getUrl());
		filterConfig.setOrder(config.getFilterOrder());
		filterConfig.setStrategy(config.getStrategy());
		filterConfig.setHttpResponseBody(config.getHttpResponseBody());
		ProxyManager<String> buckets = Bucket4j.extension(JCache.class).proxyManagerForCache(jCache(config.getCacheName(), cacheManager));
		
		config.getRateLimits().forEach(rl -> {
			ConfigurationBuilder<?> configBuilder = Bucket4j.configurationBuilder();
			for (Bucket4JBandWidth bandWidth : rl.getBandwidths()) {
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
		        	String key = getKeyFilter(rl, expressionParser, beanFactory).key(servletRequest);
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
	
	public Condition executeCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		
		if(rateLimit.getExecuteCondition() != null) {
			return  (request) -> {
				Expression expr = expressionParser.parseExpression(rateLimit.getExecuteCondition()); 
				Boolean value = expr.getValue(context, request, Boolean.class);
				return value;
			};
		}
		return null;
	}
	
	public KeyFilter getKeyFilter(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		switch(rateLimit.getFilterKeyType()) {
		case IP:
			return (request) -> request.getRemoteAddr();
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
				return value;
			};
		
		}
		return (request) -> "1";
	}
	
}
