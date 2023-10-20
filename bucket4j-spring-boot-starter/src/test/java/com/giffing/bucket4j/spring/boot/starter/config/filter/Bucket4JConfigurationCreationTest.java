package com.giffing.bucket4j.spring.boot.starter.config.filter;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class Bucket4JConfigurationCreationTest {

	private static Validator validator;

	Bucket4JConfiguration config;

	@BeforeAll
	public static void setupValidator(){
		try(ValidatorFactory factory = Validation.buildDefaultValidatorFactory()){
			validator = factory.getValidator();
		}
	}
	@BeforeEach
	public void setup() {
		config = new Bucket4JConfiguration();
	}

	@Test
	public void setFilterIdTest() {
		config.setId("id");
		assert(config.getId().equals("id"));
		validator.validateProperty(config, "id"); //validate that autoconfigure allows string values
	}
	@Test
	public void setFilterIdWithSpacesTest() {
		config.setId(" id ");
		assert(config.getId().equals("id"));
		validator.validateProperty(config, "id"); //validate that autoconfigure allows string values
	}

	@Test
	public void setFilterIdNullTest() {
		config.setId(null);
		assert (config.getId() == null);
		validator.validateProperty(config, "id"); //validate that autoconfigure allows null values
	}

	@Test
	public void setFilterIdEmptyStringTest(){
		config.setId("");
		assert (config.getId() == null);
	}

	@Test
	public void setFilterIdSpacesTest(){
		config.setId(" ");
		assert (config.getId() == null);
	}
}
