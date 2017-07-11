package com.giffing.bucket4j.spring.boot.starter.config;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface Bucket4JKeyFilter {

	public String key(HttpServletRequest request);
	
}
