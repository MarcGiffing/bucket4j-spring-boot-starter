package com.giffing.bucket4j.spring.boot.starter.config.filter.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MethodExecutePredicate<T> extends ExecutePredicate<T>  {

	private List<String> methods = new ArrayList<>();

	public boolean testRequestMethod(String requestMethod) {
		var matches = methods
			.stream()
			.filter(m -> m.equalsIgnoreCase(requestMethod))
			.findFirst();
		log.debug("method-predicate;methods:{};value:{},result:{}", methods, requestMethod, matches.isPresent());
		return matches.isPresent();
	}

	@Override
	public String name() {
		return "METHOD";
	}

	@Override
	public void parseSimpleConfig(String simpleConfig) {
		this.methods = Arrays.stream(simpleConfig.split(","))
				.map(String::trim)
				.toList();
	}

}
