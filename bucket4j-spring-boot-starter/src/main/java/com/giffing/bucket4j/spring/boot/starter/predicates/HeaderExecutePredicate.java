package com.giffing.bucket4j.spring.boot.starter.predicates;

import java.util.Arrays;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public abstract class HeaderExecutePredicate<T> extends ExecutePredicate<T> {

	private String headername;
	
	private String headerValueRegex;

	public boolean testHeaderValues(List<String> headerValues) {
		if(headerValues.isEmpty()) {
			return false;
		}
		var matches = true;
		if(headerValueRegex != null) {
			matches = headerValues
				.stream()
				.anyMatch(v -> v.matches(headerValueRegex));
		}
		
		log.debug("header-predicate;header:{};value:{},result:{}", headername, headerValues, matches);
		return matches;
	}

	@Override
	public String name() {
		return "HEADER";
	}

	@Override
	public ExecutePredicate<T> parseSimpleConfig(String simpleConfig) {
		var headerConfig = Arrays.stream(simpleConfig.split(","))
				.map(String::trim)
				.toList();
		if(headerConfig.size() > 2 || headerConfig.isEmpty()) {
			throw new IllegalArgumentException("Header Configuration failed");
		}
		this.headername = headerConfig.get(0);
		if(headerConfig.size() > 1) {
			this.headerValueRegex = headerConfig.get(1);
		}
		return this;
	}

}
