package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import java.util.Arrays;
import java.util.Collections;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HeaderExecutePredicate extends ServletRequestExecutePredicate {

	private String headername;
	
	private String headerValueRegex;

	@Override
	public boolean test(HttpServletRequest t) {
		var headerValues = Collections.list(t.getHeaders(headername))
					.stream()
					.toList();
		if(headerValues.isEmpty()) {
			return false;
		}
		var matches = true;
		if(headerValueRegex != null) {
			matches = headerValues
				.stream()
				.anyMatch(v -> v.matches(headerValueRegex));
		}
		
		log.debug("header-predicate;method:{};value:{},result:{}", t.getMethod(), headerValues, matches);
		return matches;
	}

	@Override
	public String name() {
		return "HEADER";
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
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
