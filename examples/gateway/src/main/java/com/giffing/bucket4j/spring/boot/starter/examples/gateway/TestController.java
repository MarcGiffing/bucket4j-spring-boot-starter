package com.giffing.bucket4j.spring.boot.starter.examples.gateway;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

@RestController
@RequestMapping("/")
public class TestController {

	private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

	public TestController(CacheManager<String, Bucket4JConfiguration> configCacheManager){
		this.configCacheManager = configCacheManager;
	}

	@PostMapping("filters/{filterId}")
	public ResponseEntity updateConfig(@PathVariable String filterId, @RequestBody Bucket4JConfiguration filter){
		configCacheManager.setValue(filterId, filter);
		return ResponseEntity.ok().build();
	}
}
