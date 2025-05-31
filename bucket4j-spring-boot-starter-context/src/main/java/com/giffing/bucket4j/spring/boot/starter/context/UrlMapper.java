package com.giffing.bucket4j.spring.boot.starter.context;

public interface UrlMapper {

    UrlMatcher getMatcher(String pattern);

    boolean isValid(String pattern);

}
