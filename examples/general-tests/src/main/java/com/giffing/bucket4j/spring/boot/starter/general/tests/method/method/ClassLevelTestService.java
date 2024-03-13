package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.context.IgnoreRateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RateLimiting(name = "default")
public class ClassLevelTestService {

    public void notAnnotatedMethod() {
        log.info("Method notAnnotatedMethod");
    }

    @IgnoreRateLimiting
    public void ignoreMethod() {
        log.info("Method ignoreMethod");
    }

}
