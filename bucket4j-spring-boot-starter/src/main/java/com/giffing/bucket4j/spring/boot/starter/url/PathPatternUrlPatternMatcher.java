package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternMatcher;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.Map;

import static org.springframework.http.server.PathContainer.parsePath;

@RequiredArgsConstructor
public class PathPatternUrlPatternMatcher
        implements UrlPatternMatcher {

    private final PathPattern pathPattern;

    @Override
    public boolean matches(
            String url,
            String query) {
        var pathContainer = parsePath(url);
        return pathPattern.matches(
                pathContainer);
    }

    @Override
    public Map<String, String> matchAndExtract(
            String url,
            String query) {
        final var matchInfo =
                pathPattern.matchAndExtract(
                        parsePath(url));
        return matchInfo != null ? matchInfo.getUriVariables() : null;
    }

    public static PathPatternUrlPatternMatcher create(
            String pattern) {
        var pathPatternParser =
                new PathPatternParser();
        var pathPattern =
                pathPatternParser.parse(pattern);
        return new PathPatternUrlPatternMatcher(pathPattern);
    }

}
