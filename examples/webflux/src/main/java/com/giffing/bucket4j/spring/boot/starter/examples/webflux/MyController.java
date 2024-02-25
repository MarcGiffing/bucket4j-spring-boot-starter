package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.utils.Bucket4JUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping
public class MyController {

	@Autowired
	Validator validator;

	private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

	public MyController(@Nullable CacheManager<String, Bucket4JConfiguration> configCacheManager) {
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


	/**
	 * Example of how a filter configuration can be updated during runtime
	 * @param filterId id of the filter to update
	 * @param newConfig the new filter configuration
	 * @return
	 */
	@PostMapping("filters/{filterId}")
	public ResponseEntity<?> updateConfig(
		@PathVariable String filterId,
		@RequestBody Bucket4JConfiguration newConfig) {
		if(configCacheManager == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Dynamic updating is disabled");

		//validate that the path id matches the body
		if (!newConfig.getId().equals(filterId)) {
			return ResponseEntity.badRequest().body("The id in the path does not match the id in the request body.");
		}

		//validate that there are no errors by the Jakarta validation
		Set<ConstraintViolation<Bucket4JConfiguration>> violations = validator.validate(newConfig);
		if (!violations.isEmpty()) {
			List<String> errors = violations.stream().map(ConstraintViolation::getMessage).toList();
			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Configuration validation failed", errors));
		}

		//retrieve the old config and validate that it can be replaced by the new config
		Bucket4JConfiguration oldConfig = configCacheManager.getValue(filterId);
		ResponseEntity<String> validationResponse = Bucket4JUtils.validateConfigurationUpdate(oldConfig, newConfig);
		if (validationResponse != null) {
			return validationResponse;
		}

		//insert the new config into the cache, so it will trigger the cacheUpdateListeners
		configCacheManager.setValue(filterId, newConfig);

		return ResponseEntity.ok().build();
	}

	private record ValidationErrorResponse(String message, List<String> errors) {}
}