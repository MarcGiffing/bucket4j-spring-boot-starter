package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MethodExecutePredicate extends ServletRequestExecutePredicate {

	@Override
	public ExecutePredicate<HttpServletRequest> newInstance() {
		return new MethodExecutePredicate();
	}

	@Override
	public String name() {
		return "METHOD";
	}
	
	@Override
	public boolean test(HttpServletRequest t) {
		var result = t.getMethod().equals(getValue());
		log.debug("method-predicate;method:{};value:{},result:{}", t.getMethod(), getValue(), result);
		return result;
	}

}
