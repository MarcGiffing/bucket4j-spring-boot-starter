package com.giffing.bucket4j.spring.boot.starter.exception;

/**
 * This exception should be thrown if no cache was found 
 */
public class JCacheNotFoundException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;
	
	private String cacheName;
	
	/**
	 * @param cacheName the missing cache key
	 */
	public JCacheNotFoundException(String cacheName) {
		this.cacheName = cacheName;
	}

	public String getCacheName() {
		return cacheName;
	}

	public void setCacheName(String cacheName) {
		this.cacheName = cacheName;
	}

}
