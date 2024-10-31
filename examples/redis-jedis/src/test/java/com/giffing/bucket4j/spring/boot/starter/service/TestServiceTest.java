package com.giffing.bucket4j.spring.boot.starter.service;

import com.giffing.bucket4j.spring.boot.starter.test.aop.Bucket4JAnnotationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Bucket4JAnnotationTest
@TestPropertySource("classpath:application-ratelimit.properties")
@ContextConfiguration(classes = TestService.class)
class TestServiceTest {

    @Autowired
    private TestService testService;

    @Test
    void shouldUseFallbackMethod() {

        assertEquals("Hello Horst!", testService.greetings());
        assertEquals("You are not welcome Horst!", testService.greetings());

    }

}