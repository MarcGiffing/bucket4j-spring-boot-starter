package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitException;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "bucket4j.methods[0].name=default",
        "bucket4j.methods[0].cache-name=buckets",
        "bucket4j.methods[0].rate-limit.bandwidths[0].capacity=5",
        "bucket4j.methods[0].rate-limit.bandwidths[0].time=10",
        "bucket4j.methods[0].rate-limit.bandwidths[0].unit=seconds",
        "bucket4j.methods[0].rate-limit.bandwidths[0].refill-speed=greedy",
})
@RequiredArgsConstructor
@DirtiesContext
public class MethodRateLimitTest {

    @Autowired
    private TestService testService;

    @Autowired
    private ClassLevelTestService classLevelTestService;

    @Autowired
    private IgnoreOnClassLevelTestService ignoreOnClassLevelTestService;


    @Test
    public void assert_rate_limit_with_execute_condition_matches() {
        for(int i = 0; i < 5; i++) {
            // rate limit executed because it's not the admin
            testService.withExecuteCondition("normal_user");
        }
        assertThrows(RateLimitException.class, () -> testService.withExecuteCondition("normal_user"));
    }

    @Test
    public void assert_no_rate_limit_with_execute_condition_does_not_match() {
        assertAll(() -> {
            for(int i = 0; i < 10; i++) {
                // rate limit not executed for admin parameter
                testService.withExecuteCondition("admin");
            }
        });
    }

    @Test
    public void assert_rate_limit_with_fallback_method() {
        for(int i = 0; i < 5; i++) {
            assertEquals("normal-method-executed;param:my-test", testService.withFallbackMethod("my-test"));
        }
        // no exception is thrown. fall back method is executed
        assertEquals("fallback-method-executed;param:my-test", testService.withFallbackMethod("my-test"));
    }

    @Test
    public void assert_rate_limit_with_skip_condition_does_not_match() {
        for(int i = 0; i < 5; i++) {
            // skip condition does not match. rate limit is performed
            testService.withSkipCondition("normal_user");
        }
        assertThrows(RateLimitException.class, () -> testService.withSkipCondition("normal_user"));
    }

    @Test
    public void assert_no_rate_limit_with_skip_condition_matches() {
        assertAll(() -> {
            for(int i = 0; i < 10; i++) {
                // no token consumption. admin is skipped
                testService.withSkipCondition("admin");
            }
        });
    }

    @Test
    public void assert_rate_limit_with_cache_key() {
        for(int i = 0; i < 5; i++) {
            // rate limit by parameter value
            testService.withCacheKey("key1");
            testService.withCacheKey("key2");
            // all tokens consumed
        }
        assertThrows(RateLimitException.class, () -> testService.withCacheKey("key1"));
        assertThrows(RateLimitException.class, () -> testService.withCacheKey("key2"));
    }

    @Test
    public void assert_rate_limit_with_rate_per_method() {
        for(int i = 0; i < 5; i++) {
            // rate limit by parameter value
            testService.withRatePerMethod1("key1");
            testService.withRatePerMethod2("key2");
            // all tokens consumed
        }
        assertThrows(RateLimitException.class, () -> testService.withRatePerMethod1("key1"));
        assertThrows(RateLimitException.class, () -> testService.withRatePerMethod2("key2"));
    }

    @Test
    public void assert_rate_limit_with_class_level_rate_limit() {
        for(int i = 0; i < 5; i++) {
            // rate limit executed because it's not the admin
            classLevelTestService.notAnnotatedMethod();
        }
        assertThrows(RateLimitException.class, () -> classLevelTestService.notAnnotatedMethod());
    }

    @Test
    public void assert_no_rate_limit_with_ignored_method() {
        assertAll(() -> {
            for (int i = 0; i < 20; i++) {
                // rate limit executed because it's not the admin
                classLevelTestService.ignoreMethod();
            }
        });
    }

    @Test
    public void assert_no_rate_limit_with_ignored_class() {
        assertAll(() -> {
            for (int i = 0; i < 20; i++) {
                // rate limit executed because it's not the admin
                classLevelTestService.ignoreMethod();
            }
        });
    }

}
