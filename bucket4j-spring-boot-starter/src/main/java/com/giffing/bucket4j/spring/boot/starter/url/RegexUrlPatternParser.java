package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternMatcher;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParseException;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParser;

import java.util.regex.PatternSyntaxException;

public class RegexUrlPatternParser
        implements UrlPatternParser {

    @Override
    public UrlPatternMatcher parse(
            String pattern
    ) throws UrlPatternParseException {
        try {
            return RegexUrlPatternMatcher.create(pattern);
        } catch (PatternSyntaxException e) {
            throw new UrlPatternParseException(
                    "Invalid regex pattern: " + pattern, e);
        }
    }

}
