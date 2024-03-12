package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
    MethodRateLimitTest.class
})
public class MethodTestSuite {
}
