package com.giffing.bucket4j.spring.boot.starter.context;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface KeyFilter {

	String key(HttpServletRequest type);
	
}
