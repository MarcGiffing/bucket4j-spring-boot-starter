package com.giffing.bucket4j.spring.boot.starter.general.tests.method.failures;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.exception.*;
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
import org.springframework.test.annotation.DirtiesContext;

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

    /**
     * This test ensures that a Spring Expression with a method parameter that does not exist will throw a
     * {@link RateLimitUnknownParameterException} exception and stops the application to start.
     * The test is executed for execute, skip and cache-Key expressions.
     */
    @ParameterizedTest
    @MethodSource("invalidParameter")
    @DirtiesContext
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

    /**
     * Asserts a {@link RateLimitingMethodNameNotConfiguredException} when the configuration name in the @{@link RateLimiting#name()}
     * has no property reference 'bucket4j.methods[x].name'.
     */
    @Test
    @DirtiesContext
    public void assert_startup_failure_when_cache_name_in_annotation_does_not_exist() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles("invalidMethodName");
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var methodNameNotConfiguredException = Assertions.assertThrows(RateLimitingMethodNameNotConfiguredException.class, springApplication::run);
        assertEquals("invalid_name", methodNameNotConfiguredException.getName());
        assertEquals(InvalidMethodNameExpression.class.getName(), methodNameNotConfiguredException.getClassName());
        assertEquals("testInvalidMethodName", methodNameNotConfiguredException.getMethodName());
    }

    /**
     * Asserts a {@link RateLimitingFallbackMethodNotFoundException} then the fallback method name in
     * {@link RateLimiting#fallbackMethodName()} does not exists in the same class.
     */
    @Test
    @DirtiesContext
    public void assert_startup_failure_when_fallback_method_not_exists() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles("invalidFallbackMethod");
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var exception = Assertions.assertThrows(RateLimitingFallbackMethodNotFoundException.class, springApplication::run);
        assertEquals("doesNotExist", exception.getFallbakcMethodName());
        assertEquals(InvalidFallbackMethodName.class.getName(), exception.getClassName());
        assertEquals("testFallbackMethodNotExists", exception.getMethodName());
    }

    /**
     * Asserts a {@link RateLimitingMultipleFallbackMethodsFoundException} when multiple {@link RateLimiting#fallbackMethodName()}s
     * found in the same class. Only one is allowed.
     */
    @Test
    @DirtiesContext
    public void assert_startup_failure_when_multiple_fallback_method_exists() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles("multipleFallbackMethods");
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var exception = Assertions.assertThrows(RateLimitingMultipleFallbackMethodsFoundException.class, springApplication::run);
        assertEquals("myFallbackMethod", exception.getFallbakcMethodName());
        assertEquals(MultipleFallbackMethods.class.getName(), exception.getClassName());
        assertEquals("testMultipleFallbackMethods", exception.getMethodName());
    }

    /**
     * Asserts a {@link RateLimitingFallbackReturnTypesMismatchException} when the return type of the fallback method
     * does not have the same signature of the rate limit method.
     */
    @Test
    @DirtiesContext
    public void assert_startup_failure_when_return_type_from_fallback_method_differs() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles("invalidFallbackMethodReturnType");
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var exception = Assertions.assertThrows(RateLimitingFallbackReturnTypesMismatchException.class, springApplication::run);
        assertEquals("fallback", exception.getFallbackMethodName());
        assertEquals(InvalidFallbackMethodReturnType.class.getName(), exception.getClassName());
        assertEquals("testFallbackMethodReturnTypeDiffers", exception.getMethodName());
        assertEquals("public final class java.lang.String", exception.getReturnType());
        assertEquals("public final class java.lang.Integer", exception.getFallbackMethodReturnType());
    }

    /**
     * Asserts a {@link RateLimitingFallbackMethodParameterMismatchException} when the parameters of the {@link RateLimiting#fallbackMethodName()}
     * does not have the same count and signature as the rate limit method.
     */
    @Test
    @DirtiesContext
    public void assert_startup_failure_when_parameters_from_fallback_method_differs() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        springApplication.setAdditionalProfiles("InvalidFallbackMethodParameter");
        Properties properties = getValidBucket4jProperties();
        springApplication.setDefaultProperties(properties);

        var exception = Assertions.assertThrows(RateLimitingFallbackMethodParameterMismatchException.class, springApplication::run);
        assertEquals("fallback", exception.getFallbackMethodName());
        assertEquals(InvalidFallbackMethodParameter.class.getName(), exception.getClassName());
        assertEquals("testFallbackMethodReturnTypeDiffers", exception.getMethodName());
        assertEquals("firstParam:class java.lang.String;secondParam:class java.lang.Integer", exception.getParameters());
        assertEquals("firstParam:class java.lang.String;secondParam:class java.lang.String", exception.getFallbackMethodParameters());
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

        @Bean
        @Profile("invalidFallbackMethod")
        public InvalidFallbackMethodName invalidFallbackMethodName() {
            return new InvalidFallbackMethodName();
        }

        @Bean
        @Profile("invalidFallbackMethodReturnType")
        public InvalidFallbackMethodReturnType invalidFallbackMethodReturnType() {
            return new InvalidFallbackMethodReturnType();
        }

        @Bean
        @Profile("multipleFallbackMethods")
        public MultipleFallbackMethods multipleFallbackMethods() {
            return new MultipleFallbackMethods();
        }

        @Bean
        @Profile("InvalidFallbackMethodParameter")
        public InvalidFallbackMethodParameter invalidFallbackMethodParameter() {
            return new InvalidFallbackMethodParameter();
        }


        public static void main(String[] args) {
            SpringApplication.run(com.giffing.bucket4j.spring.boot.starter.general.tests.method.method.MethodTestApplication.class, args);
        }

    }


    @NoArgsConstructor
    private static class InvalidExecuteExpression {

        @RateLimiting(name = "default", executeCondition = "#notExistingParam eq 'aaa'")
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

    @NoArgsConstructor
    private static class InvalidFallbackMethodName {

        @RateLimiting(name = "default", fallbackMethodName = "doesNotExist")
        public void testFallbackMethodNotExists(String cacheKey) {

        }
    }

    @NoArgsConstructor
    private static class MultipleFallbackMethods {

        @RateLimiting(name = "default", fallbackMethodName = "myFallbackMethod")
        public void testMultipleFallbackMethods(String cacheKey) {
        }

        public void myFallbackMethod(String cacheKey) {

        }

        public void myFallbackMethod(String cacheKey, String otherParameter) {

        }
    }

    @NoArgsConstructor
    private static class InvalidFallbackMethodReturnType {

        @RateLimiting(name = "default", fallbackMethodName = "fallback")
        public String testFallbackMethodReturnTypeDiffers(String cacheKey) {
            return "A String";
        }

        public Integer fallback(String cacheKey) {
            return 1;
        }
    }

    @NoArgsConstructor
    private static class InvalidFallbackMethodParameter {

        @RateLimiting(name = "default", fallbackMethodName = "fallback")
        public String testFallbackMethodReturnTypeDiffers(String firstParam, Integer secondParam) {
            return "A String";
        }

        public String fallback(String firstParam, String secondParam) {
            return "A String %s %s".formatted(firstParam, secondParam);
        }
    }


}
