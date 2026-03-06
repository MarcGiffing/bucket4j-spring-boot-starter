package com.giffing.bucket4j.spring.boot.starter.webflux.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.PathExecutePredicate;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;


@Component
public class WebfluxPathExecutePredicate extends PathExecutePredicate<ServerHttpRequest> {

    @Override
    public boolean test(ServerHttpRequest t) {
        return testPath(t.getPath().value());
    }

}
