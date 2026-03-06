package com.giffing.bucket4j.spring.boot.starter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.Bucket4JConfigurationPredicateNameValidator;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.servlet.predicates.ServletMethodPredicate;
import com.giffing.bucket4j.spring.boot.starter.servlet.predicates.ServletPathExecutePredicate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ConfigPredicateNameValidatorTest {

    List<ExecutePredicate<?>> executePredicates;
    Bucket4JConfigurationPredicateNameValidator validator;

    @BeforeEach
    void setup() {
		executePredicates = Arrays.asList(
				new ServletPathExecutePredicate(), new ServletMethodPredicate()
		);
        validator = new Bucket4JConfigurationPredicateNameValidator(executePredicates);
    }

    /**
     * Validate that all filtermethods pass the test if they don't have predicates
     */
    @ParameterizedTest
    @EnumSource(FilterMethod.class)
    void testValidConfigurationWithoutPredicates(FilterMethod filterMethod) {
        var configuration = setupConfiguration(filterMethod, List.of(), List.of());
        testValidPredicates(configuration);
    }

    /**
     * Validate that SERVLET pass the test with a valid PATH execute-predicate
     */
    @ParameterizedTest
    @EnumSource(value = FilterMethod.class, names = {"SERVLET"})
    void testValidServletExecutePredicate(FilterMethod filterMethod) {
        var executePredicateConfigs = List.of("PATH=valid-predicate");
        var skipPredicates = List.<String>of();
        var configuration = setupConfiguration(filterMethod,  executePredicateConfigs, skipPredicates);

        testValidPredicates(configuration);
    }

    /**
     * Validate that SERVLETpass the test with a valid PATH skip-predicate
     */
    @ParameterizedTest
    @EnumSource(value = FilterMethod.class, names = {"SERVLET"})
    void testValidServletSkipPredicate(FilterMethod filterMethod) {
        var executePredicateConfigs = List.<String>of();
        var skipPredicates = List.of("PATH=valid-predicate");
        var configuration = setupConfiguration(filterMethod,  executePredicateConfigs, skipPredicates);

        testValidPredicates(configuration);
    }

    /**
     * Validate that multiple invalid predicates give a correct error message for both SERVLET
     */
    @ParameterizedTest
    @EnumSource(value = FilterMethod.class, names = {"SERVLET"})
    void testMultipleInvalidServletPredicates(FilterMethod filterMethod) {
        var executePredicateConfigs = List.of("INVALID_EXECUTE=invalid");
        var skipPredicates = List.of("INVALID_SKIP=invalid");
        var configuration = setupConfiguration(filterMethod,  executePredicateConfigs, skipPredicates);

        var expectedInvalid = List.of("INVALID_EXECUTE", "INVALID_SKIP");
        testInvalidPredicates(configuration, getInvalidPredicateMessage(expectedInvalid));
    }

    /**
     * Validate that the servlet predicates
     */
    @Test
    void testInvalidServletPredicate() {
        var executePredicateConfigs = List.of("PATH=both-methods");
        var skipPredicates = List.of("METHOD=servlet-only");
        var servletConfiguration = setupConfiguration(FilterMethod.SERVLET, executePredicateConfigs, skipPredicates);

        testValidPredicates(servletConfiguration);
    }

    /**
     * Validate that custom predicates are supported
     */
    @Test
    void customPredicateTest() {
        List<ExecutePredicate<?>> includingCustomPredicate = new ArrayList<>(this.executePredicates);
		includingCustomPredicate.add(new CustomTestPredicate());

        var customPredicateValidator = new Bucket4JConfigurationPredicateNameValidator(includingCustomPredicate);

        var executePredicateConfigs = List.of("CUSTOM-QUERY=custom-servlet");
        var skipPredicates = List.<String>of();
        var configuration = setupConfiguration(FilterMethod.SERVLET, executePredicateConfigs, skipPredicates);

        var context = mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        assertTrue(customPredicateValidator.isValid(configuration, context));
    }

    private Bucket4JConfiguration setupConfiguration(
            FilterMethod filterMethod,
            List<String> executePredicates,
            List<String> skipPredicates
    ) {
        var configuration = new Bucket4JConfiguration();
        configuration.setFilterMethod(filterMethod);

        var rateLimit = new RateLimit();
        rateLimit.setExecutePredicates(executePredicates.stream().map(ExecutePredicateDefinition::new).toList());
        rateLimit.setSkipPredicates(skipPredicates.stream().map(ExecutePredicateDefinition::new).toList());
        configuration.setRateLimits(Collections.singletonList(rateLimit));

        return configuration;
    }

    private String getInvalidPredicateMessage(List<String> expectedInvalidNames){
        return "Invalid predicate name" + (expectedInvalidNames.size() > 1 ? "s" : "") + ": " +
                String.join(", ", expectedInvalidNames);
    }

    private void testValidPredicates(Bucket4JConfiguration configuration) {
        var context = mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        assertTrue(this.validator.isValid(configuration, context));
    }

    private void testInvalidPredicates(Bucket4JConfiguration configuration, String expectedError) {
        var context = mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        assertFalse(this.validator.isValid(configuration, context));

        var contextCaptor = ArgumentCaptor.forClass(String.class);
        verify(context).buildConstraintViolationWithTemplate(contextCaptor.capture());

        assertEquals(expectedError, contextCaptor.getValue());
    }


    private static class CustomTestPredicate extends ExecutePredicate<HttpServletRequest> {

        @Override
        public String name() {
            return "CUSTOM-QUERY";
        }

        @Override
        protected ExecutePredicate<HttpServletRequest> parseSimpleConfig(String simpleConfig) {
            return this;
        }

        @Override
        public boolean test(HttpServletRequest httpServletRequest) {
            return false;
        }
    }
}
