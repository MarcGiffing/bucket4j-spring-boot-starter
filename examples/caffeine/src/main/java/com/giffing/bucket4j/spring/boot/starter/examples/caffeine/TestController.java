package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RestController
public class TestController {

	@Autowired
	Validator validator;


	private final CacheManager<String, Bucket4JConfiguration> manager;

	public TestController(CacheResolver cacheResolver) {
		this.manager = cacheResolver.resolveConfigCacheManager("filterConfigCache");
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

//	@PostMapping("filters/{filterId}")
//	public ResponseEntity updateConfig(
//			@PathVariable String filterId,
//			@RequestBody @Valid Bucket4JConfiguration filter,
//			BindingResult bindingResult) {
//
//		if (bindingResult.hasErrors()) {
//			List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
//			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Configuration validation failed", errors));
//		}
//
//		if (manager.getValue(filterId) == null) {
//			return ResponseEntity.notFound().build();
//		}
//
//		manager.setValue(filterId, filter);
//		return ResponseEntity.ok().build();
//	}

	@PostMapping("filters/{filterId}")
	public ResponseEntity updateConfig(
			@PathVariable String filterId,
			@RequestBody @Valid Bucket4JConfiguration filter,
			BindingResult bindingResult) {

		//validate that there are no errors by the Jakarta validation
		if (bindingResult.hasErrors()) {
			List<String> errors = bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList();
			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Configuration validation failed", errors));
		}

		//validate that the id in the path matches the id in the body
		if (!filter.getId().equals(filterId)) {
			return ResponseEntity.badRequest().body("Filter id in the path does not match the request body.");
		}

		//validate that the filter exists, since creating new filters is not supported
		Bucket4JConfiguration oldConfig = manager.getValue(filterId);
		if (oldConfig == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No filter with id '" + filterId + "' could be found.");
		}

		//validate that the fields that are not allowed to change remain the same
		ResponseEntity<?> response;
		response = validateFieldEquality("filterMethod", oldConfig.getFilterMethod(), filter.getFilterMethod());
		if (response != null) return response;
		response = validateFieldEquality("filterOrder", oldConfig.getFilterOrder(), filter.getFilterOrder());
		if (response != null) return response;
		response = validateFieldEquality("cacheName", oldConfig.getCacheName(), filter.getCacheName());
		if (response != null) return response;

		//insert the new config into the cache, so it will trigger the cacheUpdateListeners
		manager.setValue(filterId, filter);
		return ResponseEntity.ok().build();
	}

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
		Bucket4JConfiguration config = manager.getValue(filterId);
		if (config == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No filter with id '" + filterId + "' could be found.");
		}
		RateLimit rl = config.getRateLimits().get(limitIndex);
		if (rl == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No ratelimit with index " + limitIndex + " could be found.");
		}
		Optional<BandWidth> bw = rl.getBandwidths().stream().filter(x -> Objects.equals(x.getId(), bandwidthId)).findFirst();
		if (bw.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No bandwidth with id '" + bandwidthId + "' could be found.");
		}

		//replace the bandwidth
		rl.getBandwidths().set(rl.getBandwidths().indexOf(bw.get()), bandWidth);

		//validate that the changed config is still valid
		Set<ConstraintViolation<Bucket4JConfiguration>> violations = this.validator.validate(config);
		if(!violations.isEmpty()){
			List<String> errors = violations.stream().map(ConstraintViolation::getMessage).toList();
			return ResponseEntity.badRequest().body(new ValidationErrorResponse("Configuration validation failed", errors));
		}

		//update the version number and insert the updated config into the cache, so it will trigger the cacheUpdateListeners
		config.setMinorVersion(config.getMinorVersion() + 1);
		manager.setValue(filterId, config);
		return ResponseEntity.ok().build();
	}

	private ResponseEntity<?> validateFieldEquality(String fieldName, Object oldValue, Object newValue) {
		if (!Objects.equals(oldValue, newValue)) {
			String errorMessage = String.format(
					"It is not possible to modify the %s of an existing filter. Expected the field to be '%s' but is '%s'.",
					fieldName, oldValue, newValue);
			return ResponseEntity.badRequest().body(errorMessage);
		}
		return null;
	}
}


