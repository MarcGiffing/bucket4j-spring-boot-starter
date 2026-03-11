package com.giffing.bucket4j.spring.boot.starter.autoconfigure.exception;

import lombok.Getter;

import java.io.Serial;

/**
 * This exception should be thrown if no cache was found
 */
@Getter
public class JCacheNotFoundException extends com.giffing.bucket4j.spring.boot.starter.core.exception.Bucket4jGeneralException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String cacheName;

    /**
     * @param cacheName the missing cache key
     */
    public JCacheNotFoundException(String cacheName) {
        super("The cache name '" + cacheName + "' defined in the property is not configured in the caching provider");
        this.cacheName = cacheName;

    }

}
