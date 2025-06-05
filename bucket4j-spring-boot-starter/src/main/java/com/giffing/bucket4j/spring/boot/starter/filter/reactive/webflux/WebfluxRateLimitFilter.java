package com.giffing.bucket4j.spring.boot.starter.filter.reactive.webflux;

import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.WebFilter;

public interface WebfluxRateLimitFilter extends WebFilter, Ordered {

	void setFilterConfig(
			FilterConfiguration<ServerHttpRequest, ServerHttpResponse> filterConfig);

}
