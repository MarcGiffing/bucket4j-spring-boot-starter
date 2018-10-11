package com.giffing.bucket4j.spring.boot.starter.webflux;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class WebfluxRateLimitException extends ResponseStatusException {

	private static final long serialVersionUID = 1L;

	public WebfluxRateLimitException(String reason) {
		super(HttpStatus.TOO_MANY_REQUESTS, reason);
	}

}
