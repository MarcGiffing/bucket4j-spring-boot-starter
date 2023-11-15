package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

	private final CacheManager<String, Bucket4JConfiguration> manager;

	public TestController(CacheResolver cacheResolver){
		manager = cacheResolver.resolveConfigCacheManager("filterConfigCache");
	}

	@GetMapping("hello")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello World");
	}

	@GetMapping("world")
	public ResponseEntity<String> world() {
		return ResponseEntity.ok("Hello World");
	}

	@PostMapping
	public ResponseEntity updateConfig(@RequestParam String filterId, @RequestParam int newCapacity){
		Bucket4JConfiguration config = manager.getValue(filterId);
		config.getRateLimits().get(0).getBandwidths().get(0).setCapacity(newCapacity);
		config.setMinorVersion(config.getMinorVersion() + 1);
		manager.setValue(config.getId(), config);
		return ResponseEntity.ok().build();
	}
}
