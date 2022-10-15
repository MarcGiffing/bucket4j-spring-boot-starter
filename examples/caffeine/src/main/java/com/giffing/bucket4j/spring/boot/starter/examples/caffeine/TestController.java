package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import javax.cache.CacheManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	
	@Autowired
	private CacheManager cachemanager;
	
	@GetMapping("hello")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello World");
	}
	
	@GetMapping("world")
	public ResponseEntity<String> world() {
		return ResponseEntity.ok("Hello World");
	}
	
	@GetMapping("clear-all-caches")
	public ResponseEntity<String> evictAllTokens() {
		cachemanager.getCacheNames().forEach(cachename -> {
			cachemanager.getCache(cachename).clear();
			System.out.println("evicted: " + cachename);
		});
		return ResponseEntity.ok("evicted");
	}
	
	
}
