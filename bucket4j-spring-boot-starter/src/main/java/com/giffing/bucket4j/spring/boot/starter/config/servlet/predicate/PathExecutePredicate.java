package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PathExecutePredicate extends ServletRequestExecutePredicate {

	@Override
	public ExecutePredicate<HttpServletRequest> newInstance() {
		return new PathExecutePredicate();
	}
	
	@Override
	public boolean test(HttpServletRequest t) {
		var result = t.getServletPath().equals(getValue());
		log.debug("path-predicate;path:{};value:{};result:{}", t.getServletPath(), getValue(), result);
		return result;
	}

	@Override
	public String name() {
		return "PATH";
	}

}
