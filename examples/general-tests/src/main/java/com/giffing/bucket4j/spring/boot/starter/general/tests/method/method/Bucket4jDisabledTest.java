package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(properties = {
        "bucket4j.enabled=false",
        "bucket4j.methods[0].name=default",
        "bucket4j.methods[0].cache-name=buckets",
        "bucket4j.methods[0].rate-limit.bandwidths[0].capacity=5",
        "bucket4j.methods[0].rate-limit.bandwidths[0].time=10",
        "bucket4j.methods[0].rate-limit.bandwidths[0].unit=seconds",
        "bucket4j.methods[0].rate-limit.bandwidths[0].refill-speed=greedy",
})
@RequiredArgsConstructor
@DirtiesContext
public class Bucket4jDisabledTest {

    @Autowired
    private TestService testService;

    @Test
    public void assert_no_rate_limit_if_bucket4j_is_disabled() {
        for(int i = 0; i < 50; i++) {
            assertAll(() -> testService.withExecuteCondition("normal_user"));
        }
    }
}
