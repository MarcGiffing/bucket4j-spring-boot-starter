package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.boot.SpringApplication;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
public class NoCacheFoundTest {

    @Test
    public void test() {
        SpringApplication springApplication = new SpringApplication(MethodTestApplication.class);
        Properties properties = new Properties();
        properties.put("bucket4j.cache-to-use", "does_not_exist");
        properties.put("bucket4j.methods[0].name", "default");
        properties.put("bucket4j.methods[0].cache-name", "buckets");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].capacity", "5");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].time", "10");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].unit", "seconds");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].refill-speed", "greedy");
        springApplication.setDefaultProperties(properties);

        var beanCreationException = Assertions.assertThrows(BeanCreationException.class, springApplication::run);
        assertNotNull(beanCreationException);
        Throwable rootCause = beanCreationException.getRootCause();
        assertNotNull(rootCause);
        assertEquals("cache not configured properly", rootCause.getMessage());
    }
}
