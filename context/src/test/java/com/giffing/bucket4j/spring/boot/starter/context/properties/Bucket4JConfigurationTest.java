package com.giffing.bucket4j.spring.boot.starter.context.properties;

import tools.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


class Bucket4JConfigurationTest {

    private static Validator validator;

    Bucket4JConfiguration config;

    @BeforeAll
    static void setupValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setup() {
        config = new Bucket4JConfiguration();
    }

    @Test
    void setFilterIdTest() {
        config.setId("id");
        assertEquals("id", config.getId());
        validator.validateProperty(config, "id"); //validate that autoconfigure allows string values
    }

    @Test
    void setFilterIdWithSpacesTest() {
        config.setId(" id ");
        assertEquals("id", config.getId());
        validator.validateProperty(config, "id"); //validate that autoconfigure allows string values
    }

    @Test
    void setFilterIdNullTest() {
        config.setId(null);
        assertNull(config.getId());
        validator.validateProperty(config, "id"); //validate that autoconfigure allows null values
    }

    @Test
    void setFilterIdEmptyStringTest() {
        config.setId("");
        assertNull(config.getId());
    }

    @Test
    void setFilterIdSpacesTest() {
        config.setId(" ");
        assertNull(config.getId());
    }

    @Test
    void serializationTest() throws JacksonException {
        //validate that the config still contains all the same data after serializing and deserializing
        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(config);
        Bucket4JConfiguration deserialized = mapper.readValue(serialized, Bucket4JConfiguration.class);
        assertThat(config).isEqualTo(deserialized);
    }

    @Test
    void invalidSerializationTest() throws JacksonException {
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
