package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CaffeineApplication {

	public static void main(String[] args) {
		SpringApplication.run(CaffeineApplication.class, args);
	}
	
}
