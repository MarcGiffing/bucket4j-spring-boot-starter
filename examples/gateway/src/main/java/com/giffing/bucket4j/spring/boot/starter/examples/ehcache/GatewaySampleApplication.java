package com.giffing.bucket4j.spring.boot.starter.examples.ehcache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@EnableConfigurationProperties(UriConfiguration.class)
@EnableCaching
public class GatewaySampleApplication {


	public static void main(String[] args) {
		SpringApplication.run(GatewaySampleApplication.class, args);
	}

	@Bean
	public RouteLocator customRouteLocator(RouteLocatorBuilder builder, UriConfiguration uriConfiguration) {
		//@formatter:off
		String httpUri = uriConfiguration.getHttpbin();
		return builder.routes()
				 .route(p -> p
				            .path("/hello")
				            .filters(f -> f.addRequestHeader("Hello", "World"))
				            .uri(httpUri))
				.build();
		//@formatter:on
	}

}
