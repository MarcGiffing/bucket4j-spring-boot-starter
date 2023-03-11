package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;

@Getter
public class ExecutePredicateBeanNotFoundException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;

	private final String executePredicateName;
	
	public ExecutePredicateBeanNotFoundException(String executePredicateName) {
		this.executePredicateName = executePredicateName;
	}
	
}
