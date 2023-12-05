package com.giffing.bucket4j.spring.boot.starter.exception;

import java.io.Serial;

import lombok.Getter;

/**
 * This exception should be thrown if no cache was found
 */
@Getter
public class JCacheNotFoundException extends Bucket4jGeneralException {

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
