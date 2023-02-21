package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WebfluxInfinispanApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxInfinispanApplication.class, args);
	}
	
}
