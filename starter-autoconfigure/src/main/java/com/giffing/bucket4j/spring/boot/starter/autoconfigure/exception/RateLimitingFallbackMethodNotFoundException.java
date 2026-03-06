package com.giffing.bucket4j.spring.boot.starter.autoconfigure.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RateLimitingFallbackMethodNotFoundException extends com.giffing.bucket4j.spring.boot.starter.core.exception.Bucket4jGeneralException {

    private final String fallbakcMethodName;

    private final String className;

    private final String methodName;

}