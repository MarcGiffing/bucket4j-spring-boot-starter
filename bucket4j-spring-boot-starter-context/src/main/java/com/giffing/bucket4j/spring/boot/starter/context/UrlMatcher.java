package com.giffing.bucket4j.spring.boot.starter.context;

@FunctionalInterface
public interface UrlMatcher {

    boolean match(String url);

}
