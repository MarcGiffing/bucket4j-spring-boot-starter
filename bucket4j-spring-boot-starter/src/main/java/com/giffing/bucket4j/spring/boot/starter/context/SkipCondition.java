package com.giffing.bucket4j.spring.boot.starter.context;

import javax.servlet.http.HttpServletRequest;

@FunctionalInterface
public interface SkipCondition {

	boolean shouldSkip(HttpServletRequest type);
	
}
