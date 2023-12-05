package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class MyController {

    private final CacheManager<String, Bucket4JConfiguration> manager;

    public MyController(CacheResolver cacheResolver){
        manager = cacheResolver.resolveConfigCacheManager("filterConfigCache");
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
        manager.setValue(filterId, filter);
        return ResponseEntity.ok().build();
    }
	
}