package com.giffing.bucket4j.spring.boot.starter.config.servlet.predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PathExecutePredicate extends ServletRequestExecutePredicate {

	private PathPatternParser pathPatternParser = new PathPatternParser();

	private List<PathPattern> pathPatterns = new ArrayList<>();
	
	@Override
	public boolean test(HttpServletRequest t) {
		PathContainer path = PathContainer.parsePath(t.getServletPath());
		var matches = pathPatterns
			.stream()
			.filter(p -> p.matches(path))
			.findFirst();
		log.debug("path-predicate;path:{};value:{};result:{}", t.getServletPath(), pathPatterns, matches.isPresent());
		return matches.isPresent();
	}

	@Override
	public String name() {
		return "PATH";
	}

	@Override
	public ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
		pathPatterns = Arrays.stream(simpleConfig.split(","))
			.map(String::trim)
			.map(pathPatternParser::parse)
			.toList();
		return this;
	}

}
