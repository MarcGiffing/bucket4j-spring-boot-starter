package com.giffing.bucket4j.spring.boot.starter.examples.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.ServletTestSuite;
import com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.ServletUpdateFilterTestSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        ServletTestSuite.class,
        ServletUpdateFilterTestSuite.class,
})
public class HazelcastGeneralSuite {
}
