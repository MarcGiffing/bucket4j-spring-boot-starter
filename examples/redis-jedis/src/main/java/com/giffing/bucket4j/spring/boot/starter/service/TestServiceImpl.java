package com.giffing.bucket4j.spring.boot.starter.service;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.servlet.IpHandlerInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class TestServiceImpl implements TestService {

    @RateLimiting(
            name = "method_test",
            cacheKey = "@testServiceImpl.getRemoteAddr()",
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

    @SuppressWarnings("unused")
    public String getRemoteAddr() {
        try {
            return (String) RequestContextHolder.currentRequestAttributes().getAttribute(IpHandlerInterceptor.IP, RequestAttributes.SCOPE_REQUEST);
        } catch (IllegalStateException e) {
            return "0.0.0.0";
        }
    }
}
