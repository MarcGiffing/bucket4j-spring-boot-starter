package com.giffing.bucket4j.spring.boot.starter.webflux;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.Bucket4JConfigurationPredicateNameValidator;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.webflux.predicates.WebfluxHeaderExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.webflux.predicates.WebfluxPathExecutePredicate;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigPredicateNameValidatorTest {

    List<ExecutePredicate<?>> executePredicates;
    Bucket4JConfigurationPredicateNameValidator validator;

    @BeforeEach
    void setup() {
        executePredicates = Arrays.asList(
                new WebfluxPathExecutePredicate(), new WebfluxHeaderExecutePredicate()
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
     * Validate that WEBFLUX pass the test with a valid PATH execute-predicate
     */
    @ParameterizedTest
    @EnumSource(value = FilterMethod.class, names = {"WEBFLUX"})
    void testValidWebfluxExecutePredicate(FilterMethod filterMethod) {
        var executePredicates = List.of("PATH=valid-predicate");
        var skipPredicates = List.<String>of();
        var configuration = setupConfiguration(filterMethod, executePredicates, skipPredicates);

        testValidPredicates(configuration);
    }

    /**
     * Validate that WEBFLUX pass the test with a valid PATH skip-predicate
     */
    @ParameterizedTest
    @EnumSource(value = FilterMethod.class, names = {"WEBFLUX"})
    void testValidWebfluxSkipPredicate(FilterMethod filterMethod) {
        var executePredicates = List.<String>of();
        var skipPredicates = List.of("PATH=valid-predicate");
        var configuration = setupConfiguration(filterMethod, executePredicates, skipPredicates);

        testValidPredicates(configuration);
    }


    /**
     * Validate that multiple invalid predicates give a correct error message for both WEBFLUX
     */
    @ParameterizedTest
    @EnumSource(value = FilterMethod.class, names = {"WEBFLUX"})
    void testMultipleInvalidWebfluxPredicates(FilterMethod filterMethod) {
        List<String> executePredicates = List.of("INVALID_EXECUTE=invalid");
        List<String> skipPredicates = List.of("INVALID_SKIP=invalid");
        Bucket4JConfiguration configuration = setupConfiguration(filterMethod, executePredicates, skipPredicates);

        List<String> expectedInvalid = List.of("INVALID_EXECUTE", "INVALID_SKIP");
        testInvalidPredicates(configuration, getInvalidPredicateMessage(expectedInvalid));
    }

    /**
     * Validate that the webflux predicates don't give false positives for servlet filters
     */
    @Test
    void testInvalidWebfluxPredicate() {
        var executePredicates = List.of("PATH=both-methods");
        var skipPredicates = List.of("HEADER=webflux-only");
        var webfluxConfiguration = setupConfiguration(FilterMethod.WEBFLUX, executePredicates, skipPredicates);

        testValidPredicates(webfluxConfiguration);
    }

    /**
     * Validate that the servlet predicates don't give false positives for webflux filters
     */
    @Test
    void testInvalidWebfluxWithServletPredicate() {
        var executePredicates = List.of("PATH=both-methods");
        var skipPredicates = List.of("METHOD=servlet-only");
        var webfluxConfiguration = setupConfiguration(FilterMethod.WEBFLUX, executePredicates, skipPredicates);

        var expectedInvalid = List.of("METHOD");
        testInvalidPredicates(webfluxConfiguration, getInvalidPredicateMessage(expectedInvalid));
    }

    private Bucket4JConfiguration setupConfiguration(
            FilterMethod filterMethod,
            List<String> executePredicates,
            List<String> skipPredicates
    ) {
        var configuration = new Bucket4JConfiguration();
        configuration.setFilterMethod(filterMethod);

        var rateLimit = new RateLimit();
        rateLimit.setExecutePredicates(executePredicates.stream().map(ExecutePredicateDefinition::new).collect(Collectors.toList()));
        rateLimit.setSkipPredicates(skipPredicates.stream().map(ExecutePredicateDefinition::new).collect(Collectors.toList()));
        configuration.setRateLimits(Collections.singletonList(rateLimit));

        return configuration;
    }

    private String getInvalidPredicateMessage(List<String> expectedInvalidNames) {
        return "Invalid predicate name" + (expectedInvalidNames.size() > 1 ? "s" : "") + ": " +
                String.join(", ", expectedInvalidNames);
    }

    private void testValidPredicates(Bucket4JConfiguration configuration) {
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        assertTrue(this.validator.isValid(configuration, context));
    }

    private void testInvalidPredicates(Bucket4JConfiguration configuration, String expectedError) {
        ConstraintValidatorContext context = Mockito.mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
        Assertions.assertFalse(this.validator.isValid(configuration, context));

        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(context).buildConstraintViolationWithTemplate(contextCaptor.capture());

        assertEquals(expectedError, contextCaptor.getValue());
    }


}
