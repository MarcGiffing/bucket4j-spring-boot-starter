package com.giffing.bucket4j.spring.boot.starter.examples.gateway;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping
public class MyController {

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
	
}