package com.giffing.bucket4j.spring.boot.starter.utils;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

public class Bucket4JUtils {

	public static ResponseEntity<String> validateConfigurationUpdate(Bucket4JConfiguration oldConfig, Bucket4JConfiguration newConfig){
		if (oldConfig == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No filter with id '" + newConfig.getId() + "' could be found.");
		}

		//validate that the version increased
		if (oldConfig.getBucket4JVersionNumber() >= newConfig.getBucket4JVersionNumber()) {
			return ResponseEntity.badRequest().body("The new configuration should have a higher version than the current configuration.");
		}

		//validate that the fields that are not allowed to change remain the same
		ResponseEntity<String> response;
		response = validateFieldEquality("filterMethod", oldConfig.getFilterMethod(), newConfig.getFilterMethod());
		if (response != null) return response;
		response = validateFieldEquality("filterOrder", oldConfig.getFilterOrder(), newConfig.getFilterOrder());
		if (response != null) return response;
		return validateFieldEquality("cacheName", oldConfig.getCacheName(), newConfig.getCacheName());
	}

	private static ResponseEntity<String> validateFieldEquality(String fieldName, Object oldValue, Object newValue) {
		if (!Objects.equals(oldValue, newValue)) {
			String errorMessage = String.format(
					"It is not possible to modify the %s of an existing filter. Expected the field to be '%s' but is '%s'.",
					fieldName, oldValue, newValue);
			return ResponseEntity.badRequest().body(errorMessage);
		}
		return null;
	}
}
