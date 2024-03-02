package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ServletTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServletTestApplication.class, args);
	}
	
}
