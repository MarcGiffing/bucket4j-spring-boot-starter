package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class MyController {

	private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

	public MyController(CacheManager<String, Bucket4JConfiguration> configCacheManager){
		this.configCacheManager = configCacheManager;
	}

	@GetMapping("/hello")
    public Mono<String> hello(
        @RequestParam(defaultValue = "World") String name) {
        return Mono.just("Hello")
            .flatMap(s -> Mono
                .just(s + ", " + name + "!\n")
            );
    }
	
	@GetMapping("/world")
    public Mono<String> world(
        @RequestParam(defaultValue = "World") String name) {
        return Mono.just("Hello")
            .flatMap(s -> Mono
                .just(s + ", " + name + "!\n")
            );
    }

	@PostMapping("filters/{filterId}")
	public ResponseEntity updateConfig(@PathVariable String filterId, @RequestBody Bucket4JConfiguration filter){
		configCacheManager.setValue(filterId, filter);
		return ResponseEntity.ok().build();
	}
}