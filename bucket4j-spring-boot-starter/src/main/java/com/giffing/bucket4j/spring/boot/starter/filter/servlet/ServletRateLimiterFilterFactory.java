package com.giffing.bucket4j.spring.boot.starter.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface ServletRateLimiterFilterFactory {

    ServletRateLimitFilter create(
            FilterConfiguration<HttpServletRequest, HttpServletResponse> filterConfig);

}
