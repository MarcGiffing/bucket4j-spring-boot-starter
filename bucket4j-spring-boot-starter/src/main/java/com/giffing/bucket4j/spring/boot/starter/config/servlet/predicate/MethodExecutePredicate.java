package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MethodExecutePredicate extends ServletRequestExecutePredicate {

	private String method;

	@Override
	public boolean test(HttpServletRequest t) {
		var result = t.getMethod().equals(method);
		log.debug("method-predicate;method:{};value:{},result:{}", t.getMethod(), method, result);
		return result;
	}

	@Override
	public String name() {
		return "METHOD";
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
		this.method = simpleConfig;
		return this;
	}

}
