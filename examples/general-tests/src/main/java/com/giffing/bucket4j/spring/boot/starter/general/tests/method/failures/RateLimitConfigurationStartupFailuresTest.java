package com.giffing.bucket4j.spring.boot.starter.general.tests.method.failures;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.exception.RateLimitUnknownParameterException;
import com.giffing.bucket4j.spring.boot.starter.exception.RateLimitingMethodNameNotConfiguredException;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


public class RateLimitConfigurationStartupFailuresTest {

    private static Stream<Arguments> invalidParameter() {
        return Stream.of(
                Arguments.of("executeExpression", "#notExistingParam eq 'aaa'", InvalidExecuteExpression.class.getName(), "testInvalidExecuteExpression", "existingParam"),
                Arguments.of("skipExpression", "#skipExpression eq 'aaa'", InvalidSkipExpression.class.getName(), "testInvalidSkipExpression", "skipExpressionParam"),
                Arguments.of("cacheKeyExpression", "#cacheKeyX", InvalidCacheKeyExpression.class.getName(), "testCacheKeyExpression", "cacheKey")

        );
    }

    @ParameterizedTest
    @MethodSource("invalidParameter")
    public void assert_startup_failure_when_execute_expression_has_invalid_method_parameter(
            String profile, String expression, String className, String methodName, String parameters
    ) {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles(profile);
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var unknownParameterException = Assertions.assertThrows(RateLimitUnknownParameterException.class, springApplication::run);
        assertNotNull(unknownParameterException);
        assertEquals(expression, unknownParameterException.getExpression());
        assertEquals(className, unknownParameterException.getClassName());
        assertEquals(methodName, unknownParameterException.getMethodName());
        assertEquals(parameters, String.join(",", unknownParameterException.getMethodParameter()));
    }

    @Test
    public void assert_startup_failure_when_execute_expression_has_invalid_method_parameter() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles("invalidMethodName");
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var methodNameNotConfiguredException = Assertions.assertThrows(RateLimitingMethodNameNotConfiguredException.class, springApplication::run);
        assertEquals("invalid_name", methodNameNotConfiguredException.getName());
        assertEquals(InvalidMethodNameExpression.class.getName(), methodNameNotConfiguredException.getClassName());
        assertEquals("testInvalidMethodName", methodNameNotConfiguredException.getMethodName());
        assertEquals("default", String.join(",", methodNameNotConfiguredException.getAvailableNames()));

    }

    private static Properties getValidBucket4jProperties() {
        Properties properties = new Properties();
        properties.put("bucket4j.methods[0].name", "default");
        properties.put("bucket4j.methods[0].cache-name", "buckets");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].capacity", "5");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].time", "10");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].unit", "seconds");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].refill-speed", "greedy");
        return properties;
    }

    @SpringBootApplication
    @EnableCaching
    @EnableAspectJAutoProxy
    public static class MyInternalApplication {

        @Bean
        @Profile("executeExpression")
        public InvalidExecuteExpression invalidExecuteExpression() {
            return new InvalidExecuteExpression();
        }

        @Bean
        @Profile("skipExpression")
        public InvalidSkipExpression invalidSkipExpression() {
            return new InvalidSkipExpression();
        }

        @Bean
        @Profile("cacheKeyExpression")
        public InvalidCacheKeyExpression invalidCacheKeyExpression() {
            return new InvalidCacheKeyExpression();
        }

        @Bean
        @Profile("invalidMethodName")
        public InvalidMethodNameExpression invalidMethodNameExpression() {
            return new InvalidMethodNameExpression();
        }

        public static void main(String[] args) {
            SpringApplication.run(com.giffing.bucket4j.spring.boot.starter.general.tests.method.method.MethodTestApplication.class, args);
        }

    }


    @NoArgsConstructor
    private static class InvalidExecuteExpression {

        @RateLimiting(name = "default", executeCondition = "'fg' eq 'aaa'")
        public void testInvalidExecuteExpression(String existingParam) {

        }
    }

    @NoArgsConstructor
    private static class InvalidSkipExpression {

        @RateLimiting(name = "default", executeCondition = "#skipExpression eq 'aaa'")
        public void testInvalidSkipExpression(String skipExpressionParam) {

        }
    }

    @NoArgsConstructor
    private static class InvalidCacheKeyExpression {

        @RateLimiting(name = "default", cacheKey = "#cacheKeyX")
        public void testCacheKeyExpression(String cacheKey) {

        }
    }

    @NoArgsConstructor
    private static class InvalidMethodNameExpression {

        @RateLimiting(name = "invalid_name", cacheKey = "#cacheKeyX")
        public void testInvalidMethodName(String cacheKey) {

        }
    }


}
