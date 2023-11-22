package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import lombok.Getter;

import java.util.List;

@Getter
public class ValidationErrorResponse {
	private final String message;
	private final List<String> errors;

	public ValidationErrorResponse(String message, List<String> errors) {
		this.message = message;
		this.errors = errors;
	}

}
