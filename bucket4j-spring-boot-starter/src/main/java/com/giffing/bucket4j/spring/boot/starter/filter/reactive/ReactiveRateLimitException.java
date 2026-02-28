package com.giffing.bucket4j.spring.boot.starter.filter.reactive;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.Serial;

public class ReactiveRateLimitException extends ResponseStatusException {

    @Serial private static final long serialVersionUID = 1L;

	public ReactiveRateLimitException(HttpStatus httpStatusCode, String reason) {
		super(httpStatusCode, reason);
	}

}
