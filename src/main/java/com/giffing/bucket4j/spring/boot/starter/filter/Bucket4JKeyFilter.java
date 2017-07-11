package com.giffing.bucket4j.spring.boot.starter.filter;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface Bucket4JKeyFilter {

	public String key(HttpServletRequest request);
	
}
