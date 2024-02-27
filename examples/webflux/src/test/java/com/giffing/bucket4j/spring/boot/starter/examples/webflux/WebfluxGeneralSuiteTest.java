package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import com.giffing.bucket4j.spring.boot.starter.general.tests.filter.reactive.WebfluxTestSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
       WebfluxTestSuite.class
})
public class WebfluxGeneralSuiteTest {
}
