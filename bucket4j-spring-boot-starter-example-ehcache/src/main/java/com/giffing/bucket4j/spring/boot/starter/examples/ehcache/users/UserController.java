package com.giffing.bucket4j.spring.boot.starter.examples.ehcache.users;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class UserController {

	@GetMapping
	public ResponseEntity<User> user() {
		return ResponseEntity.ok(User.builder().firstname("Frodo").lastname("Beutlin").build());
	}
	
}
