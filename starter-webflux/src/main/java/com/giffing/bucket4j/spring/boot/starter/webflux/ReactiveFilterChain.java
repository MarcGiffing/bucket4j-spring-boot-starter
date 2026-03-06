package com.giffing.bucket4j.spring.boot.starter.webflux;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface ReactiveFilterChain {
    Mono<Void> apply(ServerWebExchange exchange);
}
