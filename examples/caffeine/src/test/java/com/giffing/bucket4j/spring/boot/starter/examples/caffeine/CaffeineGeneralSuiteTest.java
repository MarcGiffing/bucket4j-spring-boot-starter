package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import com.giffing.bucket4j.spring.boot.starter.general.tests.filter.method.MethodTestSuite;
import com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.ServletTestSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        ServletTestSuite.class,
        MethodTestSuite.class
})
public class CaffeineGeneralSuiteTest {
}
