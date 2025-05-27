package com.giffing.bucket4j.spring.boot.starter.context;

@FunctionalInterface
public interface UrlTemplateMapper {
    /**
     * Converts a URL template into a regular expression.
     *
     * @param template the URL template to convert
     * @return the regular expression representation of the URL template
     */
    String map(String template);

}
