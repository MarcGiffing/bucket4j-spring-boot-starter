package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TestService {


    @RateLimiting(name = "default",
            executeCondition = "#myParamName != 'admin'",
            ratePerMethod = true,
            fallbackMethodName = "dummy")
    public String execute(String myParamName) {
        log.info("Method with Param {} executed", myParamName);
        return myParamName;
    }

    public String dummy(String myParamName) {
        log.info("Fallback-Method with Param {} executed", myParamName);
        return myParamName;
    }

}
