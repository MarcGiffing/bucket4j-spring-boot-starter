package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestService {


    @RateLimiting(
            name = "default",
            executeCondition = "#myParamName != 'admin'")
    public String withExecuteCondition(String myParamName) {
        log.info("Method withExecuteCondition with Param {} executed", myParamName);
        return myParamName;
    }

    @RateLimiting(
            name = "default",
            skipCondition = "#myParamName eq 'admin'")
    public String withSkipCondition(String myParamName) {
        log.info("Method withSkipCondition with Param {} executed", myParamName);
        return myParamName;
    }

    @RateLimiting(
            name = "default",
            cacheKey = "#cacheKey")
    public String withCacheKey(String cacheKey) {
        log.info("Method withCacheKey with Param {} executed", cacheKey);
        return cacheKey;
    }

    @RateLimiting(
            name = "default",
            ratePerMethod = true)
    public String withRatePerMethod1(String cacheKey) {
        log.info("Method withRatePerMethod1 with Param {} executed", cacheKey);
        return cacheKey;
    }

    @RateLimiting(
            name = "default",
            ratePerMethod = true)
    public String withRatePerMethod2(String cacheKey) {
        log.info("Method withRatePerMethod1 with Param {} executed", cacheKey);
        return cacheKey;
    }


    @RateLimiting(name = "default", cacheKey = "'normal'", fallbackMethodName = "fallbackMethod")
    public String withFallbackMethod(String myParamName) {
        return "normal-method-executed;param:" + myParamName;
    }

    public String fallbackMethod(String myParamName) {
        return "fallback-method-executed;param:" + myParamName;
    }

}
