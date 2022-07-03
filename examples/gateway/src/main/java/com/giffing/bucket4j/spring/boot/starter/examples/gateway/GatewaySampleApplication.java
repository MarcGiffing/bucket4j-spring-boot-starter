package com.giffing.bucket4j.spring.boot.starter.examples.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableCaching
public class GatewaySampleApplication {


	public static void main(String[] args) {
		SpringApplication.run(GatewaySampleApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
		//@formatter:off
		return builder.routes()
				 .route(p -> p
				            .path("/get")
				            .filters(f -> f.addRequestHeader("Hello", "World"))
				            .uri("http://httpbin.org:80"))
				.build();
		//@formatter:on
	}

}
