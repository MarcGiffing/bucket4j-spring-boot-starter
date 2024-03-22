package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.context.IgnoreRateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * No rate limiting should be executed on any method if the class is annotated with @{@link IgnoreRateLimiting}
 */
@Component
@Slf4j
@IgnoreRateLimiting
public class IgnoreOnClassLevelTestService {

    @RateLimiting(name = "default")
    public void execute() {
        log.info("Method execute");
    }

}
