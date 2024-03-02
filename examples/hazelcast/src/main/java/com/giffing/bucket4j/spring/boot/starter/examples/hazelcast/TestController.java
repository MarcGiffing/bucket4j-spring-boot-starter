package com.giffing.bucket4j.spring.boot.starter.examples.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.utils.Bucket4JUtils;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import java.util.List;

@RestController
@RequestMapping("/")
public class TestController {

	private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

	public TestController(@Nullable CacheManager<String, Bucket4JConfiguration> configCacheManager) {
		this.configCacheManager = configCacheManager;
	}

	@GetMapping("hello")
	public ResponseEntity<String> helloWorld() {
		return ResponseEntity.ok().body("Hello World");
	}


	/**
	 * Example of how a filter configuration can be updated during runtime
	 * @param filterId id of the filter to update
	 * @param newConfig the new filter configuration
	 * @param bindingResult the result of the Jakarta validation
	 * @return
	 */
	@PostMapping("filters/{filterId}")
	public ResponseEntity<?> updateConfig(
		@PathVariable String filterId,
		@RequestBody @Valid Bucket4JConfiguration newConfig,
		BindingResult bindingResult) {
		if(configCacheManager == null) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Dynamic updating is disabled");

		//validate that the path id matches the body
		if (!newConfig.getId().equals(filterId)) {
			return ResponseEntity.badRequest().body("The id in the path does not match the id in the request body.");
		}

		//validate that there are no errors by the Jakarta validation
		if (bindingResult.hasErrors()) {
			List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
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
