package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.general.tests.method.failures.RateLimitConfigurationStartupFailuresTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        MethodRateLimitTest.class,
        NoCacheFoundTest.class,
        RateLimitConfigurationStartupFailuresTest.class
})
public class MethodTestSuite {
}
