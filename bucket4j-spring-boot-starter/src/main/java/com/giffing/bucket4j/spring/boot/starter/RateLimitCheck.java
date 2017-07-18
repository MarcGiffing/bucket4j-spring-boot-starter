package com.giffing.bucket4j.spring.boot.starter;

import javax.servlet.http.HttpServletRequest;

import io.github.bucket4j.ConsumptionProbe;

@FunctionalInterface
public interface RateLimitCheck {

	ConsumptionProbe rateLimit(HttpServletRequest request);
	
}
