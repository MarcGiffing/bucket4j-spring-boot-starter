package com.giffing.bucket4j.spring.boot.starter.config.failureanalyzer;

import com.giffing.bucket4j.spring.boot.starter.exception.*;
import org.springframework.boot.diagnostics.AbstractFailureAnalyzer;
import org.springframework.boot.diagnostics.FailureAnalysis;

/**
 * The failure analyzer is responsible to provide readable information of exception which
 * occur on startup. All exception based on the {@link Bucket4jGeneralException} are handled here.
 */
public class Bucket4JAutoConfigFailureAnalyzer extends AbstractFailureAnalyzer<Bucket4jGeneralException> {

    public static final String NEW_LINE = System.lineSeparator();

    @Override
    protected FailureAnalysis analyze(Throwable rootFailure, Bucket4jGeneralException cause) {
        String descriptionMessage = cause.getMessage();
        String actionMessage = cause.getMessage();

        if (cause instanceof JCacheNotFoundException e) {
            descriptionMessage = e.getMessage();
            actionMessage = "Cache name: " + e.getCacheName() + NEW_LINE
                    + "Please configure your caching provider (ehcache, hazelcast, ...)";
        }

        if (cause instanceof ExecutePredicateInstantiationException e) {
            descriptionMessage = e.getMessage();
            actionMessage = "Please provide a default constructor.";
        }

        if (cause instanceof NoCacheConfiguredException e) {
            descriptionMessage = "No Bucket4j cache configuration found - cache-to-use: %s".formatted(e.getCacheToUse());
            actionMessage = """
                Please provide a valid cache configuration.
                Check the documentation to see which caches are supported.
                Check the example projects.
                Use the debug=true property to determine which Bucket4j cache configuration has problems.
                Provide your own SyncCacheResolver or AsyncCacheResolver.
            """;
        }

        if (cause instanceof RateLimitUnknownParameterException e) {
            descriptionMessage = "Your expression contains parameters which does not exists in your method";
            actionMessage = """
                your expression: %s
                available method parameter: %s
                class name: %s
                method name: %s
            """.formatted(e.getExpression(), String.join(", ", e.getMethodParameter()), e.getClassName(), e.getMethodName());
        }

        if (cause instanceof RateLimitingMethodNameNotConfiguredException e) {
            descriptionMessage = "Your name in @RateLimiting(name =\"your name\") is not configured in your properties";
            actionMessage = """
                your name: %s
                available method configs: %s
                class name: %s
                method name: %s
            """.formatted(e.getName(), String.join(",", e.getAvailableNames()), e.getClassName(), e.getMethodName());
        }


        return new FailureAnalysis(descriptionMessage, actionMessage, cause);
    }

}
