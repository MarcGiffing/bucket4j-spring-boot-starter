package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
@Getter
public class RateLimitingMethodNameNotConfiguredException extends Bucket4jGeneralException {

    private final String name;

    private final Set<String> availableNames;

    private final String className;

    private final String methodName;

}