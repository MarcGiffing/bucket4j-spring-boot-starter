package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import com.giffing.bucket4j.spring.boot.starter.general.tests.filter.reactive.WebfluxTestSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.springframework.test.context.ActiveProfiles;

@Suite
@SelectClasses({
       WebfluxTestSuite.class
})
@ActiveProfiles("webflux-infinispan")
public class WebfluxGeneralSuiteTest {
}
