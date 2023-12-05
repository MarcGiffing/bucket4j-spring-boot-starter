package com.giffing.bucket4j.spring.boot.starter.context;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ValidationException;
import jakarta.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Validated
@Getter
@Setter
@Slf4j
@NoArgsConstructor
public class ExecutePredicateDefinition implements Serializable {

	public static final String SIMPLE_CONFIG_KEY = "_simple_config_";
	
	@NotNull
	private String name;
	
	private final Map<String, String> args = new LinkedHashMap<>();
	
	public ExecutePredicateDefinition(String name) {
		int eqIdx = name.indexOf('=');
		if (eqIdx <= 0) {
			throw new ValidationException(
					"Unable to parse ExecutePredicateDefinition text '" + name + "'" + ", must be of the form name=value");
		}
		this.name = name.substring(0, eqIdx);
		var result = name.substring(eqIdx + 1, name.length());
		this.args.put(SIMPLE_CONFIG_KEY, result);
		log.debug("execute-predicate-simple-config;name:{};value:{}", this.name, result);
	}
	
}
