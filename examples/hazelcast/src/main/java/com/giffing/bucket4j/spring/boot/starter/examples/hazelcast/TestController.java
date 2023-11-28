package com.giffing.bucket4j.spring.boot.starter.examples.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class TestController {

	private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

	public TestController(CacheManager<String, Bucket4JConfiguration> configCacheManager) {
		this.configCacheManager = configCacheManager;
	}

	@GetMapping("hello")
	public ResponseEntity helloWorld() {
		return ResponseEntity.ok().body("Hello World");
	}

	@PostMapping("filters/{filterId}")
	public ResponseEntity updateConfig(@PathVariable String filterId, @RequestBody Bucket4JConfiguration filter) {
		configCacheManager.setValue(filterId, filter);
		return ResponseEntity.ok().build();
	}


}
