package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternMatcher;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

@RequiredArgsConstructor
public class RegexUrlPatternMatcher
        implements UrlPatternMatcher {

    private final Pattern pattern;
    private final Set<String> variables;

    @Override
    public boolean matches(
            String path,
            String query) {
        return pattern.matcher(path).matches();
    }

    @Override
    public Map<String, String> matchAndExtract(
            String path,
            String query) {
        var matcher = pattern.matcher(path);
        if (!matcher.matches()) {
            return null;
        }

        var result = new HashMap<String, String>();
        for (var groupName : variables) {
            result.put(groupName, matcher.group(groupName));
        }
        return result;
    }

    public static RegexUrlPatternMatcher create(
            String pattern) {
        return new RegexUrlPatternMatcher(
                Pattern.compile(pattern),
                extractGroupNames(pattern)
        );
    }

    private static Set<String> extractGroupNames(
            String regex) {
        var groupNames = new HashSet<String>();
        var m =
                Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9_]*)>").matcher(regex);
        while (m.find()) {
            groupNames.add(m.group(1));
        }
        return groupNames;
    }

}
