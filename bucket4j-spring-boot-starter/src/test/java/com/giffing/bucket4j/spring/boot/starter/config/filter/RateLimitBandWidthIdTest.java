package com.giffing.bucket4j.spring.boot.starter.config.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jakarta.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.RateLimitBandWidthIdsValidator;
import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;

import io.github.bucket4j.TokensInheritanceStrategy;

public class RateLimitBandWidthIdTest {

    final RateLimitBandWidthIdsValidator validator = new RateLimitBandWidthIdsValidator();
    ConstraintValidatorContext context;

    @BeforeEach
    void setup(){
        context = Mockito.mock(ConstraintValidatorContext.class, Mockito.RETURNS_DEEP_STUBS);
    }

    /**
     * Validate that when a RateLimit only has 1 bandwidth, the bandwidth id can be null, no matter the TokensInheritanceStrategy
     * @param inheritanceStrategy
     */
    @ParameterizedTest
    @EnumSource(TokensInheritanceStrategy.class)
    void testValidSingleBandwidthNoId(TokensInheritanceStrategy inheritanceStrategy) {
        RateLimit rateLimit = createRateLimit(inheritanceStrategy, Collections.singletonList(null));
        assertTrue(validator.isValid(rateLimit, context));
    }

    /**
     * Validate that when a RateLimit only has 1 bandwidth, the bandwidth id can be set, no matter the TokensInheritanceStrategy
     * @param inheritanceStrategy
     */
    @ParameterizedTest
    @EnumSource(TokensInheritanceStrategy.class)
    void testValidSingleBandwidthWithID(TokensInheritanceStrategy inheritanceStrategy) {
        RateLimit rateLimit = createRateLimit(inheritanceStrategy, Collections.singletonList("id"));
        assertTrue(validator.isValid(rateLimit, context));
    }

    /**
     * Validate that a RateLimit can contain multiple bandwidths without id when using TokensInheritanceStrategy.RESET
     */
    @Test
    void testValidResetStrategyMultipleBandwidthsNoId() {
        RateLimit rateLimit = createRateLimit(TokensInheritanceStrategy.RESET, Arrays.asList(null, null, "bw1"));
        assertTrue(validator.isValid(rateLimit, context));
    }

    /**
     * Validate that a RateLimit can NOT contain multiple bandwidths without id when any other TokensInheritanceStrategy than RESET is used
     */
    @ParameterizedTest
    @EnumSource(value = TokensInheritanceStrategy.class, mode = EnumSource.Mode.EXCLUDE, names = "RESET")
    void testInValidStrategiesMultipleBandwidthsNoId(TokensInheritanceStrategy inheritanceStrategy) {
        RateLimit rateLimit = createRateLimit(inheritanceStrategy, Arrays.asList(null, null, "bw1"));
        assertFalse(validator.isValid(rateLimit, context));


        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(context).buildConstraintViolationWithTemplate(contextCaptor.capture());

        String expectedError = "Multiple bandwidths without id detected. This is only allowed when TokenInheritanceStrategy 'RESET' is applied.";
        assertEquals(expectedError, contextCaptor.getValue());
    }

    /**
     * Validate that a RateLimit can contain multiple BandWidths with unique Ids, no matter the TokensInheritanceStrategy
     */
    @ParameterizedTest
    @EnumSource(TokensInheritanceStrategy.class)
    void testValidMultipleBandwidthsUniqueIds(TokensInheritanceStrategy inheritanceStrategy) {
        RateLimit rateLimit = createRateLimit(inheritanceStrategy, Arrays.asList(null, "bw1", "bw2", "bandwidth3"));
        assertTrue(validator.isValid(rateLimit, context));
    }

    /**
     * Validate that a RateLimit cannot contain multiple BandWidths with the same id, no matter the TokensInheritanceStrategy
     */
    @ParameterizedTest
    @EnumSource(TokensInheritanceStrategy.class)
    void testInvalidDuplicateIds(TokensInheritanceStrategy inheritanceStrategy) {
        RateLimit rateLimit = createRateLimit(inheritanceStrategy, Arrays.asList("bw1", "bw1"));
        assertFalse(validator.isValid(rateLimit, context));


        ArgumentCaptor<String> contextCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(context).buildConstraintViolationWithTemplate(contextCaptor.capture());

        String expectedError = "Duplicate bandwidth id: bw1";
        assertEquals(expectedError, contextCaptor.getValue());
    }

    private RateLimit createRateLimit(TokensInheritanceStrategy inheritanceStrategy, List<String> bandwidthIds){
        RateLimit rateLimit = new RateLimit();
        rateLimit.setTokensInheritanceStrategy(inheritanceStrategy);

        List<BandWidth> bandWidths = new ArrayList<>();
        for (String id: bandwidthIds){
            BandWidth bandWidth = new BandWidth();
            bandWidth.setId(id);
            bandWidths.add(bandWidth);
        }
        rateLimit.setBandwidths(bandWidths);

        return rateLimit;
    }
}
