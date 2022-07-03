package com.giffing.bucket4j.spring.boot.starter.examples.hazelcast;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {
	
	@GetMapping
	public ResponseEntity helloWorld() {
		return ResponseEntity.ok().build();
	}
	
	
	
	
}
