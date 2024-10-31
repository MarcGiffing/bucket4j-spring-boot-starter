package com.giffing.bucket4j.spring.boot.starter.service;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.servlet.IpHandlerInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class TestService {

    private static final String name = "Horst";

    @RateLimiting(
            name = "method_test",
            cacheKey = "@testService.getRemoteAddr()",
            ratePerMethod = true,
            fallbackMethodName = "greetingsFallback"
    )
    public String greetings() {
        return String.format("Hello %s!", name);
    }

    @SuppressWarnings("unused")
    public String greetingsFallback() {
        return String.format("You are not welcome %s!", name);
    }

    @SuppressWarnings("unused")
    public String getRemoteAddr() {
        try {
            return (String) RequestContextHolder.currentRequestAttributes().getAttribute(IpHandlerInterceptor.IP, RequestAttributes.SCOPE_REQUEST);
        } catch (IllegalStateException e) {
            return "0.0.0.0";
        }
    }
}
