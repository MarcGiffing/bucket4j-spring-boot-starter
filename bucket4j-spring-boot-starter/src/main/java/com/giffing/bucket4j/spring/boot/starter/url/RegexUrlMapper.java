package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlMatcher;
import com.giffing.bucket4j.spring.boot.starter.context.UrlMapper;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexUrlMapper
        implements UrlMapper {

    @Override
    public UrlMatcher getMatcher(String pattern) {
        return (String url) ->
                url.matches(pattern);
    }

    @Override
    public boolean isValid(String pattern) {
        try {
            Pattern.compile(pattern);
            return !pattern.equals("/*");
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

}
