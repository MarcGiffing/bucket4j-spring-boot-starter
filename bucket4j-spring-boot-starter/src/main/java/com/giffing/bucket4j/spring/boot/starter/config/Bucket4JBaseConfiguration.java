package com.giffing.bucket4j.spring.boot.starter.config;

import javax.cache.Cache;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.Bucket4JBootProperties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.config.zuul.Bucket4JAutoConfigurationZuul;
import com.giffing.bucket4j.spring.boot.starter.context.KeyFilter;
import com.giffing.bucket4j.spring.boot.starter.context.SkipCondition;

import io.github.bucket4j.grid.GridBucketState;

/**
 * Holds helper Methods which are reused by the {@link Bucket4JAutoConfigurationServletFilter} and 
 * the {@link Bucket4JAutoConfigurationZuul} configuration classes
 */
public abstract class Bucket4JBaseConfiguration {
	
	@SuppressWarnings("unchecked")
	public Cache<String, GridBucketState> jCache(String cacheName, CacheManager cacheManager) {
        org.springframework.cache.Cache springCache = cacheManager.getCache(cacheName);
        if(springCache == null) {
        	throw new IllegalStateException("Please provide a cache with the name " + cacheName);
        }
		
        return (Cache<String, GridBucketState>) springCache.getNativeCache();
    }
	
	public SkipCondition filterCondition(Bucket4JConfiguration config, ExpressionParser expressionParser, BeanFactory beanFactory) {
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		
		if(config.getSkipCondition() != null) {
			return  (request) -> {
				Expression expr = expressionParser.parseExpression(config.getSkipCondition()); 
				Boolean value = expr.getValue(context, request, Boolean.class);
				return value;
			};
		}
		return null;
	}
	
	public KeyFilter getKeyFilter(Bucket4JConfiguration config, ExpressionParser expressionParser, BeanFactory beanFactory) {
		switch(config.getFilterKeyType()) {
		case IP:
			return (request) -> request.getRemoteAddr();
		case EXPRESSION:
			String expression = config.getExpression();
			if(StringUtils.isEmpty(expression)) {
				throw new IllegalArgumentException("Missing property expression for filter type expression");
			}
			StandardEvaluationContext context = new StandardEvaluationContext();
			context.setBeanResolver(new BeanFactoryResolver(beanFactory));
			return  (request) -> {
				//TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
				Expression expr = expressionParser.parseExpression(config.getExpression()); 
				final String value = expr.getValue(context, request, String.class);
				return value;
			};
		
		}
		return (request) -> "1";
	}
	
}
