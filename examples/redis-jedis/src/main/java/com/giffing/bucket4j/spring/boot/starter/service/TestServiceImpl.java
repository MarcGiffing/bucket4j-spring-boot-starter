package com.giffing.bucket4j.spring.boot.starter.service;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import org.springframework.stereotype.Component;

@Component
public class TestServiceImpl implements TestService {

    @RateLimiting(
            name = "method_test",
            cacheKey = "#ip",
            ratePerMethod = true,
            fallbackMethodName = "greetingsFallback"
    )
    @Override
    public String greetings(String name) {
        return "Hello " + name;
    }

    @SuppressWarnings("unused")
    public String greetingsFallback(String name) {
        return "You are not welcome " + name;
    }
}
