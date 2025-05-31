package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlMatcher;
import com.giffing.bucket4j.spring.boot.starter.context.UrlMapper;
import org.springframework.web.util.pattern.PathPatternParser;

import static org.springframework.http.server.PathContainer.parsePath;

public class PatternUrlMapper
        implements UrlMapper {

    private final PathPatternParser pathPatternParser =
            new PathPatternParser();

    @Override
    public UrlMatcher getMatcher(String pattern) {
        var pathPattern =
                pathPatternParser.parse(pattern);
        return (String url) ->
                pathPattern.matches(
                        parsePath(url));
    }

    @Override
    public boolean isValid(String pattern) {
        try {
            pathPatternParser.parse(pattern);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
