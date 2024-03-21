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
            descriptionMessage = """
                Your Spring Expression contains parameters which does not exists in your method;
                    your expression: %s
                    available method parameter: %s
                    class name: %s
                    method name: %s
            """.formatted(e.getExpression(), String.join(", ", e.getMethodParameter()), e.getClassName(), e.getMethodName());
            actionMessage = """
                Please update your expression and use one of the available method parameters.
                """;
        }

        if (cause instanceof RateLimitingMethodNameNotConfiguredException e) {
            descriptionMessage = """
                Your name in @RateLimiting(name ="%s") is not configured in your properties
                    your name: %s
                    available method configs: %s
                    class name: %s
                    method name: %s
                """.formatted(e.getName(), e.getName(), String.join(",", e.getAvailableNames()), e.getClassName(), e.getMethodName());
            actionMessage = "Please check four property configuration bucket4j.methods[x].name for the reference in your annotation";

        }

        if (cause instanceof RateLimitingFallbackMethodNotFoundException e) {
            descriptionMessage = """
            Your fallback method name in @RateLimiting(fallbackMethodName="%s") was not found
                your fallback method name: %s
                class name: %s
                method name: %s
            """.formatted(e.getFallbakcMethodName(), e.getFallbakcMethodName(),e.getClassName(), e.getMethodName());
            actionMessage = "Ensure that the fallback method exists in the same class";
        }

        if (cause instanceof RateLimitingMultipleFallbackMethodsFoundException e) {
            descriptionMessage = """
                Multiple fallback method names found. The fallback method name should be unique.
                    your fallback method name: %s
                    class name: %s
                    method name: %s
                """.formatted(e.getFallbakcMethodName(),e.getClassName(), e.getMethodName());
            actionMessage = "Please provide only one fallback method under the given name.";
        }

        if (cause instanceof RateLimitingFallbackReturnTypesMismatchException e) {
            descriptionMessage = """
            The return type of the fallback method does not match the rate limit method
                your fallback method name: %s
                class name: %s
                method name: %s
                return type: %s
                fallback method return type: %s
            """.formatted(e.getFallbackMethodName(),e.getClassName(), e.getMethodName(), e.getReturnType(), e.getFallbackMethodReturnType());
            actionMessage = "Please update the return type of the fallback method.";
        }

        if (cause instanceof RateLimitingFallbackMethodParameterMismatchException e) {
            descriptionMessage = """
            The parameters of the fallback method does not match the rate limit method parameters
                your fallback method name: %s
                class name: %s
                method name: %s
                parameters: %s
                fallback method parameters: %s
            """.formatted(e.getFallbackMethodName(),e.getClassName(), e.getMethodName(), e.getParameters(), e.getFallbackMethodParameters());
            actionMessage = "Please use the same parameter signature for your fallback method like in the rate limit method.";
        }

        return new FailureAnalysis(descriptionMessage, actionMessage, cause);
    }

}
