package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.reactive;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        ReactiveRateLimitTest.class,
        ReactiveGreadyRefillSpeedTest.class,
        ReactiveIntervalRefillSpeedTest.class,
})
public class WebfluxTestSuite {
}
