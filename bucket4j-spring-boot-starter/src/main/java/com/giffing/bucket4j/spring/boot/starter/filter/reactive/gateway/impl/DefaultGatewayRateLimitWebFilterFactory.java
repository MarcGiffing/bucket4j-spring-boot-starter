package com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.impl;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.GatewayRateLimitFilter;
import com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway.GatewayRateLimitFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

public class DefaultGatewayRateLimitWebFilterFactory
        implements GatewayRateLimitFilterFactory {

    public GatewayRateLimitFilter create(
            FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig) {
        return new DefaultGatewayRateLimitFilter(filterConfig);
    }

}
