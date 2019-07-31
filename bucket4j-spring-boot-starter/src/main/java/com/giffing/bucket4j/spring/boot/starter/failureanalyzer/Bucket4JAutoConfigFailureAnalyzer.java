package com.giffing.bucket4j.spring.boot.starter.failureanalyzer;

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

import com.giffing.bucket4j.spring.boot.starter.exception.Bucket4jGeneralException;
import com.giffing.bucket4j.spring.boot.starter.exception.FilterKeyTypeDeprectatedException;
import com.giffing.bucket4j.spring.boot.starter.exception.FilterURLInvalidException;
import com.giffing.bucket4j.spring.boot.starter.exception.JCacheNotFoundException;
import com.giffing.bucket4j.spring.boot.starter.exception.MissingKeyFilterExpressionException;

/**
 * The failure analyzer is responsible to provide readable information of exception which
 * occur on startup. All exception based on the {@link Bucket4jGeneralException} are handled here.   
 */
public class Bucket4JAutoConfigFailureAnalyzer extends AbstractFailureAnalyzer<Bucket4jGeneralException>{

	public static String newline = System.getProperty("line.separator");
	
	@Override
	protected FailureAnalysis analyze(Throwable rootFailure, Bucket4jGeneralException cause) {
		String descriptionMessage = cause.getMessage();
		String actionMessage = cause.getMessage();
		
		if(cause instanceof JCacheNotFoundException) {
			JCacheNotFoundException ex = (JCacheNotFoundException) cause;
			descriptionMessage = "The cache name name defined in the property is not configured in the caching provider";
			
			actionMessage = "Cache name: " + ex.getCacheName() + newline
					+ "Please configure your caching provider (ehcache, hazelcast, ...)";
		}
		
		if(cause instanceof MissingKeyFilterExpressionException) {
			descriptionMessage = "You've set the 'filter-key-type' to 'expression' but didn't set the property 'expression'";
			actionMessage = "Please set the property 'expression' in your configuration file with a valid expression (see Spring Expression Language)" + newline;
		}
		
		if( cause instanceof FilterURLInvalidException) {
			FilterURLInvalidException e = (FilterURLInvalidException) cause;
			descriptionMessage = "You've set an invalid regular expression in the 'filter'" + newline +
					"Description: " + e.getDescription();
			actionMessage = "To Filter for all requests type .* or remove the property 'bucket4j.filters.url' completly." + newline;
		}
		
		if( cause instanceof FilterKeyTypeDeprectatedException) {
			FilterKeyTypeDeprectatedException e = (FilterKeyTypeDeprectatedException) cause;
			descriptionMessage = "The filter key type support is deprecated. The expression type is set as default" + newline;
			actionMessage = "If you've set the filter key type to IP then use getRemoteAddress() for the expression." + newline;
		}
		
		
		return new FailureAnalysis(descriptionMessage, actionMessage, cause);
	}

}
