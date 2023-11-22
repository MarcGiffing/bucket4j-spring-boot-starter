package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;

@Getter
public class ExecutePredicateBeanNotFoundException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;

	private final String executePredicateName;
	
	public ExecutePredicateBeanNotFoundException(String executePredicateName) {
		super("You've configured the '%s' execution predicate which doesn't exists.".formatted(executePredicateName));
		this.executePredicateName = executePredicateName;
	}
	
}
