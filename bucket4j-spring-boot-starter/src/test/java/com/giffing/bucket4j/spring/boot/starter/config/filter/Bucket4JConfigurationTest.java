package com.giffing.bucket4j.spring.boot.starter.config.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;


public class Bucket4JConfigurationTest {

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
	void setFilterIdTest() {
		config.setId("id");
		assert(config.getId().equals("id"));
		validator.validateProperty(config, "id"); //validate that autoconfigure allows string values
	}
	@Test
	void setFilterIdWithSpacesTest() {
		config.setId(" id ");
		assert(config.getId().equals("id"));
		validator.validateProperty(config, "id"); //validate that autoconfigure allows string values
	}

	@Test
	void setFilterIdNullTest() {
		config.setId(null);
		assert (config.getId() == null);
		validator.validateProperty(config, "id"); //validate that autoconfigure allows null values
	}

	@Test
	void setFilterIdEmptyStringTest(){
		config.setId("");
		assert (config.getId() == null);
	}

	@Test
	void setFilterIdSpacesTest(){
		config.setId(" ");
		assert (config.getId() == null);
	}

	@Test
	void serializationTest() throws JsonProcessingException {
		//validate that the config still contains all the same data after serializing and deserializing
		ObjectMapper mapper = new ObjectMapper();
		String serialized = mapper.writeValueAsString(config);
		Bucket4JConfiguration deserialized = mapper.readValue(serialized, Bucket4JConfiguration.class);
		assertThat(config).isEqualTo(deserialized);
	}

	@Test
	void invalidSerializationTest() throws JsonProcessingException {
		config.getRateLimits().add(new RateLimit());
		config.getRateLimits().get(0).setBandwidths(Collections.singletonList(new BandWidth()));
		config.getRateLimits().get(0).getBandwidths().get(0).setCapacity(10);

		ObjectMapper mapper = new ObjectMapper();
		String serialized = mapper.writeValueAsString(config);
		Bucket4JConfiguration deserialized = mapper.readValue(serialized, Bucket4JConfiguration.class);

		//validate that the isEqual fails when a nested object is invalid
		deserialized.getRateLimits().get(0).getBandwidths().get(0).setCapacity(1);
		assertThat(config).isNotEqualTo(deserialized);
	}
}
