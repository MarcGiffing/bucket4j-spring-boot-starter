package com.giffing.bucket4j.spring.boot.starter.context;

import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Validated
@Getter
@Setter
public class ExecutePredicateDefinition {

	@NotNull
	private String name;
	
	@NotNull
	private String configValue;
	
	public ExecutePredicateDefinition(String name, String configValue) {
		if(name.contains("=")) {
			var splittedName = name.split("=");
			this.name = splittedName[0];
			this.configValue = splittedName[1];
		} else {
			this.name = name;
			this.configValue = configValue;
		}
	}
	
}
