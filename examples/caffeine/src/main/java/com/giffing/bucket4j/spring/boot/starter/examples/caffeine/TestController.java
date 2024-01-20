package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.utils.Bucket4JUtils;
import org.springframework.web.util.HtmlUtils;

@RestController
public class TestController {

	private final Validator validator;

	private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

	public TestController(Validator validator, @Nullable CacheManager<String, Bucket4JConfiguration> configCacheManager) {
		this.validator = validator;
		this.configCacheManager = configCacheManager;
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

	/**
	 * note: The recommended way of updating rate limits is by sending the whole Bucket4JConfiguration (see above).
	 *
	 * This endpoint is added as an example how partial data of a configuration could be updated.
	 * This should only be done if you know what you are doing, since it requires additional checks
	 * and configuring to prevent corrupting the cache. If unsure, use the example above.
	 *
	 * @param filterId The id of the filter to update
	 * @param limitIndex The index number of the RateLimit (these don't have an id, so has to be index based)
	 * @param bandwidthId The id of the bandwidth to update
	 * @param bandWidth The new BandWidth configuration
	 * @return
	 */
	@PostMapping("filters/{filterId}/ratelimits/{limitIndex}/bandwidths/{bandwidthId}")
	public ResponseEntity updateBandwidth(
			@PathVariable String filterId,
			@PathVariable int limitIndex,
			@PathVariable String bandwidthId,
			@RequestBody BandWidth bandWidth) {

		//validate that the path matches the body
		if (!bandWidth.getId().equals(bandwidthId)) {
			return ResponseEntity.badRequest().body("Bandwidth id in the path does not match the request body.");
		}

		//validate that the filter, ratelimit and bandwidth all exist
		Bucket4JConfiguration config = configCacheManager.getValue(filterId);
		if (config == null) {
			String errorMessage = "No filter with id '" + filterId + "' could be found.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HtmlUtils.htmlEscape(errorMessage));
		}
		RateLimit rl = config.getRateLimits().get(limitIndex);
		if (rl == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No ratelimit with index " + limitIndex + " could be found.");
		}
		Optional<BandWidth> bw = rl.getBandwidths().stream().filter(x -> Objects.equals(x.getId(), bandwidthId)).findFirst();
		if (bw.isEmpty()) {
			String errorMessage = "No bandwidth with id '" + bandwidthId + "' could be found.";
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(HtmlUtils.htmlEscape(errorMessage));
		}

		//replace the bandwidth
		rl.getBandwidths().set(rl.getBandwidths().indexOf(bw.get()), bandWidth);

		//validate that the changed config is still valid
		Set<ConstraintViolation<Bucket4JConfiguration>> violations = this.validator.validate(config);
		if (!violations.isEmpty()) {
			List<String> errors = violations.stream().map(ConstraintViolation::getMessage).toList();
			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Configuration validation failed", errors));
		}

		//update the version number and insert the updated config into the cache, so it will trigger the cacheUpdateListeners
		config.setMinorVersion(config.getMinorVersion() + 1);
		configCacheManager.setValue(filterId, config);

		return ResponseEntity.ok().build();
	}

	private record ValidationErrorResponse(String message, List<String> errors) {}
}
