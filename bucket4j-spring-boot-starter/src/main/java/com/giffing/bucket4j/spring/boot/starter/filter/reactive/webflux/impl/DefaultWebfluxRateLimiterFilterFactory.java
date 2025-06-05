package com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.impl;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxRateLimitFilter;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux.WebfluxRateLimiterFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

public class DefaultWebfluxRateLimiterFilterFactory
        implements WebfluxRateLimiterFilterFactory {

    public WebfluxRateLimitFilter create(
            FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig) {
        return new DefaultWebfluxRateLimitFilter(filterConfig);
    }

}
