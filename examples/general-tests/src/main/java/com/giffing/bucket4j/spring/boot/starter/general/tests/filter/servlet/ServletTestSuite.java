package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        ServletRateLimitTest.class,
        GreadyRefillSpeedTest.class,
        IntervalRefillSpeedTest.class,
        PostExecuteConditionTest.class
})
public class ServletTestSuite {
}
