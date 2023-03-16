package com.giffing.bucket4j.spring.boot.starter.predicates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.server.PathContainer;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class PathExecutePredicate<T> extends ExecutePredicate<T> {

	private PathPatternParser pathPatternParser = new PathPatternParser();

	private List<PathPattern> pathPatterns = new ArrayList<>();
	
	public boolean testPath(String servletPath) {
		PathContainer path = PathContainer.parsePath(servletPath);
		var matches = pathPatterns
			.stream()
			.filter(p -> p.matches(path))
			.findFirst();
		log.debug("path-predicate;path:{};value:{};result:{}", servletPath, pathPatterns, matches.isPresent());
		return matches.isPresent();
	}

	@Override
	public String name() {
		return "PATH";
	}

	@Override
	public ExecutePredicate<T> parseSimpleConfig(String simpleConfig) {
		pathPatterns = Arrays.stream(simpleConfig.split(","))
			.map(String::trim)
			.map(pathPatternParser::parse)
			.toList();
		return this;
	}

}
