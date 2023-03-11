package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class ServletRequestExecutePredicate implements ExecutePredicate<HttpServletRequest> {

	private String value;
	
	public ExecutePredicate<HttpServletRequest> setValue(String value) {
		this.value = value;
		return this;
	}
	
}
