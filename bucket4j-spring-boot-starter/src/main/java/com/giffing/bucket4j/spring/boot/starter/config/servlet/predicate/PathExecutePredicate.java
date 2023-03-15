package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PathExecutePredicate extends ServletRequestExecutePredicate {

	private String path;

	@Override
	public boolean test(HttpServletRequest t) {
		var result = t.getServletPath().equals(path);
		log.debug("path-predicate;path:{};value:{};result:{}", t.getServletPath(), path, result);
		return result;
	}

	@Override
	public String name() {
		return "PATH";
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
		this.path = simpleConfig;
		return this;
	}

}
