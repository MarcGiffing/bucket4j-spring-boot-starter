package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RateLimitingFallbackReturnTypesMismatchException extends Bucket4jGeneralException {

    private final String fallbackMethodName;

    private final String className;

    private final String methodName;

    private final String returnType;

    private final String fallbackMethodReturnType;

}