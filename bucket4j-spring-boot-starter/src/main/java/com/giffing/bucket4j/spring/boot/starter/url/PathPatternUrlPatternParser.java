package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternMatcher;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParseException;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

public class PathPatternUrlPatternParser
        implements UrlPatternParser {

    @Override
    public UrlPatternMatcher parse(
            String pattern
    ) throws UrlPatternParseException {
        try {
            return PathPatternUrlPatternMatcher.create(pattern);
        } catch (PatternParseException ex) {
            throw new UrlPatternParseException(
                    "Invalid path pattern: " + pattern, ex);
        }
    }

}
