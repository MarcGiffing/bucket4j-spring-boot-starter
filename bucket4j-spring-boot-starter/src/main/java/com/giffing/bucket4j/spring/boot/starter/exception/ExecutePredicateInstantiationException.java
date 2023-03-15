package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;

@Getter
public class ExecutePredicateInstantiationException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;

	private final String executePredicateName;
	
	private final Class<?> instantiationException;
	
	public ExecutePredicateInstantiationException(String executePredicateName, Class<?> instantiationException) {
		this.executePredicateName = executePredicateName;
		this.instantiationException = instantiationException;
	}
	
}
