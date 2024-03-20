package com.giffing.bucket4j.spring.boot.starter.general.tests.method.failures;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.exception.RateLimitUnknownParameterException;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class InvalidParameterInExpressionTest {

    @Test
    public void assert_startup_failure_when_cache_not_configured() {
        SpringApplication springApplication = new SpringApplication(MyInternalApplication.class);
        Properties properties = new Properties();
        properties.put("bucket4j.methods[0].name", "default");
        properties.put("bucket4j.methods[0].cache-name", "buckets");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].capacity", "5");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].time", "10");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].unit", "seconds");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].refill-speed", "greedy");
        springApplication.setDefaultProperties(properties);

        var unknownParameterException = Assertions.assertThrows(RateLimitUnknownParameterException.class, springApplication::run);
        assertNotNull(unknownParameterException);
        assertEquals("#notExistingParam eq 'aaa'", unknownParameterException.getExpression());
        assertEquals("com.giffing.bucket4j.spring.boot.starter.general.tests.method.failures.InvalidParameterInExpressionTest$InternalService", unknownParameterException.getClassName());
        assertEquals("test", unknownParameterException.getMethodName());
        assertEquals("existingParam", String.join(",", unknownParameterException.getMethodParameter()));
    }

    @SpringBootApplication
    @EnableCaching
    @EnableAspectJAutoProxy
    public static class MyInternalApplication {

        @Bean
        public InternalService internalService() {
            return new InternalService();
        }

        public static void main(String[] args) {
            SpringApplication.run(com.giffing.bucket4j.spring.boot.starter.general.tests.method.method.MethodTestApplication.class, args);
        }

    }


    @NoArgsConstructor
    private static class InternalService {

        @RateLimiting(name = "default", executeCondition = "#notExistingParam eq 'aaa'")
        public void test(String existingParam) {

        }
    }


}
