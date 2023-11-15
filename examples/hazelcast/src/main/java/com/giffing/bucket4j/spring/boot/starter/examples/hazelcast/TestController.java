package com.giffing.bucket4j.spring.boot.starter.examples.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import io.github.bucket4j.Bucket;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@RestController
@RequestMapping("/")
public class TestController {

	private final CacheManager<String, Bucket4JConfiguration> manager;

	public TestController(CacheResolver cacheResolver){
		manager = cacheResolver.resolveConfigCacheManager("filterConfigCache");
	}

	@GetMapping
	public ResponseEntity helloWorld() {
		return ResponseEntity.ok().build();
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
