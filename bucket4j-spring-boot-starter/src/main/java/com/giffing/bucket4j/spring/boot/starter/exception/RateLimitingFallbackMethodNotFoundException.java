package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RateLimitingFallbackMethodNotFoundException extends Bucket4jGeneralException {

    private final String fallbakcMethodName;

    private final String className;

    private final String methodName;

}