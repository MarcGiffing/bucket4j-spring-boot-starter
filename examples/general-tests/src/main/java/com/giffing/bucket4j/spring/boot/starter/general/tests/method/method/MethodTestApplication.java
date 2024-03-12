package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
public class MethodTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(MethodTestApplication.class, args);
	}
	
}