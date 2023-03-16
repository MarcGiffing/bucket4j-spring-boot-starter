package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MethodExecutePredicate extends ServletRequestExecutePredicate {

	private List<String> methods = new ArrayList<>();

	@Override
	public boolean test(HttpServletRequest t) {
		var requestMethod = t.getMethod();
		var matches = methods
			.stream()
			.filter(m -> m.equalsIgnoreCase(requestMethod))
			.findFirst();
		log.debug("method-predicate;method:{};value:{},result:{}", t.getMethod(), requestMethod, matches.isPresent());
		return matches.isPresent();
	}

	@Override
	public String name() {
		return "METHOD";
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
		this.methods = Arrays.stream(simpleConfig.split(","))
				.map(String::trim)
				.toList();
		return this;
	}

}
