package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * This exception is thrown when the rate limit is not configured correctly
 */
@RequiredArgsConstructor
@Getter
public class RateLimitUnknownParameterException extends Bucket4jGeneralException {

    private final String expression;

    private final String className;

    private final String methodName;

    private final Set<String> methodParameter;


}
