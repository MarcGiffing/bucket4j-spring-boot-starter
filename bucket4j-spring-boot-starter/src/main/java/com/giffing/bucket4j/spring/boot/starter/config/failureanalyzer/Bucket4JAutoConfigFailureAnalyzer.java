package com.giffing.bucket4j.spring.boot.starter.config.failureanalyzer;

import com.giffing.bucket4j.spring.boot.starter.exception.Bucket4jGeneralException;
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateInstantiationException;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * The failure analyzer is responsible to provide readable information of exception which
 * occur on startup. All exception based on the {@link Bucket4jGeneralException} are handled here.   
 */
public class Bucket4JAutoConfigFailureAnalyzer extends AbstractFailureAnalyzer<Bucket4jGeneralException>{

	public static final String NEW_LINE = System.getProperty("line.separator");
	
	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, Bucket4jGeneralException cause) {
		String descriptionMessage = cause.getMessage();
		String actionMessage = cause.getMessage();
		
		if(cause instanceof JCacheNotFoundException e) {
			descriptionMessage = e.getMessage();
			actionMessage = "Cache name: " + e.getCacheName() + NEW_LINE
					+ "Please configure your caching provider (ehcache, hazelcast, ...)";
		}
		
		if (cause instanceof ExecutePredicateInstantiationException e) {
			descriptionMessage = e.getMessage();
			actionMessage = "Please provide a default constructor.";
		}
		
		return new FailureAnalysis(descriptionMessage, actionMessage, cause);
	}

}
