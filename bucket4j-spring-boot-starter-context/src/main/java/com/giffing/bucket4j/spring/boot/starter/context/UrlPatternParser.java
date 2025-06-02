package com.giffing.bucket4j.spring.boot.starter.context;


public interface UrlPatternParser {

    UrlPatternMatcher parse(
            String pattern
    ) throws UrlPatternParseException;

}
