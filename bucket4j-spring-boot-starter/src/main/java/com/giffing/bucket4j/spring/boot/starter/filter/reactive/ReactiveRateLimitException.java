package com.giffing.bucket4j.spring.boot.starter.filter.reactive;

import java.io.Serial;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ReactiveRateLimitException extends ResponseStatusException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ReactiveRateLimitException(String reason) {
		super(HttpStatus.TOO_MANY_REQUESTS, reason);
	}

}
