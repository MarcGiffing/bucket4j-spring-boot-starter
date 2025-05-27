package com.giffing.bucket4j.spring.boot.starter.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import jakarta.servlet.Filter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;

public interface ServletRateLimitFilter extends Filter, Ordered {

    void setFilterConfig(FilterConfiguration<HttpServletRequest, HttpServletResponse> filterConfig);

}
