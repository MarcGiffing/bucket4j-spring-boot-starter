package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.filter.Bucket4JConfigurationPredicateValidator;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.exception.Bucket4jGeneralException;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
public class TestController {

	private final CacheManager<String, Bucket4JConfiguration> manager;
	private final Bucket4JConfigurationPredicateValidator configValidator;

	public TestController(CacheResolver cacheResolver, Bucket4JConfigurationPredicateValidator configValidator) {
		this.manager = cacheResolver.resolveConfigCacheManager("filterConfigCache");
		this.configValidator = configValidator;
	}

	@GetMapping("unsecure")
	public ResponseEntity unsecure() {
		return ResponseEntity.ok().build();
	}

	@GetMapping("hello")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello World");
	}

	@GetMapping("world")
	public ResponseEntity<String> world() {
		return ResponseEntity.ok("Hello World");
	}

	@PostMapping("filters/{filterId}")
	public ResponseEntity updateConfig(
			@PathVariable String filterId,
			@RequestBody @Valid Bucket4JConfiguration filter,
			BindingResult bindingResult) {

		if (bindingResult.hasErrors()) {
			List<String> errors = bindingResult.getFieldErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Validation failed", errors));
		}

		try {
			this.configValidator.validatePredicates(filter);
		} catch (Bucket4jGeneralException e) {
			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Validation failed", Collections.singletonList(e.getMessage())));
		}

		if (manager.getValue(filterId) == null) {
			return ResponseEntity.notFound().build();
		}

		manager.setValue(filterId, filter);
		return ResponseEntity.ok().build();
	}
}
