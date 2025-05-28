package com.giffing.bucket4j.spring.boot.starter.filter.reactive.gateway;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

public interface GatewayRateLimitFilter
        extends GlobalFilter, Ordered {

    void setFilterConfig(
            FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig);

}
