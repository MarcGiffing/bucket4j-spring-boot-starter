package com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.predicate;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;

@Configuration
@ComponentScan
@ConditionalOnClass(ServerHttpRequest.class)
public class WebfluxExecutePredicateConfiguration {
}
