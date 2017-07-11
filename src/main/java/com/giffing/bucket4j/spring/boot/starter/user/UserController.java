package com.giffing.bucket4j.spring.boot.starter.user;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

	@GetMapping("{id}")
	public ResponseEntity<User> getById(@PathVariable("id") Long id) {
		return ResponseEntity.ok(User.builder().firstname("Marc").lastname("Giffing").build());
	}
	
}
