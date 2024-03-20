package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;

/**
 * This exception should be thrown if no cache configuration was found.
 */
@Getter
public class NoCacheConfiguredException extends Bucket4jGeneralException {

    private final String cacheToUse;

    public NoCacheConfiguredException(String cacheToUse) {
        this.cacheToUse = cacheToUse;
    }
}
