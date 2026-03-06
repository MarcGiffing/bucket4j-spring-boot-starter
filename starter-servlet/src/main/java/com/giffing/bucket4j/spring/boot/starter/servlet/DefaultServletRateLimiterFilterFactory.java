package com.giffing.bucket4j.spring.boot.starter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DefaultServletRateLimiterFilterFactory implements ServletRateLimiterFilterFactory {

    public ServletRateLimitFilter create(FilterConfiguration<HttpServletRequest, HttpServletResponse> filterConfig) {
        return new DefaultServletRateLimitFilter(filterConfig);
    }

}
