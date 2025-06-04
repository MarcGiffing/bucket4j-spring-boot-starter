package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.Map;

public interface UrlPatternMatcher {

    boolean matches(
            String path, String query);

    Map<String, String> matchAndExtract(
            String path, String query);

    @SuppressWarnings("unused")
    UrlPatternMatcher ALLOW_MATCHER =
            new UrlPatternMatcher() {

                @Override
                public boolean matches(
                        String path,
                        String query) {
                    return true;
                }

                @Override
                public Map<String, String> matchAndExtract(
                        String path,
                        String query) {
                    return Map.of();
                }
            };

    @SuppressWarnings("unused")
    UrlPatternMatcher DENY_MATCHER =
            new UrlPatternMatcher() {

                @Override
                public boolean matches(
                        String path,
                        String query) {
                    return false;
                }

                @Override
                public Map<String, String> matchAndExtract(
                        String path,
                        String query) {
                    return null;
                }
            };

}
