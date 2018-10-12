package com.giffing.bucket4j.spring.boot.starter.exception;

public class FilterURLInvalidException extends Bucket4jGeneralException {

	private static final long serialVersionUID = 1L;

	private final String filterRegex;

	private final String description;
	
	public FilterURLInvalidException(String filterRegex, String description) {
		this.filterRegex = filterRegex;
		this.description = description;
	}

	public String getFilterRegex() {
		return filterRegex;
	}

	public String getDescription() {
		return description;
	}
	
}
