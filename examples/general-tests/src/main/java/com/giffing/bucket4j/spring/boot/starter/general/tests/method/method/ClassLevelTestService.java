package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.context.IgnoreRateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Used to test a @{@link RateLimiting} annotation on class level.
 */
@Component
@Slf4j
@RateLimiting(name = "default")
public class ClassLevelTestService {

    public void notAnnotatedMethod() {
        log.info("Method notAnnotatedMethod");
    }

    /**
     * Method should be ignored from rate limiting.
     */
    @IgnoreRateLimiting
    public void ignoreMethod() {
        log.info("Method ignoreMethod");
    }

}
