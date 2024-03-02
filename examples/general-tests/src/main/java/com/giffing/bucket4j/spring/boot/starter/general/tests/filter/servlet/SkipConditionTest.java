package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.IntStream;

import static com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.MockMvcHelper.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "logging.level.com.giffing.bucket4j=debug",
        "bucket4j.filters[0].cache-name=buckets",
        "bucket4j.filters[0].url=.*",
        "bucket4j.filters[0].rate-limits[0].skip-condition=getHeader('user') eq 'admin'",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
})
@AutoConfigureMockMvc
@DirtiesContext
public class SkipConditionTest {

    public static final String HEADER_USER = "user";
    public static final String TEST_URL = "/hello";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DirtiesContext
    void request_is_blocked_when_header_username_is_not_admin() throws Exception {
        String url = TEST_URL;
        IntStream.rangeClosed(1, 5)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach(counter -> webRequestWithStatus(mockMvc, url, HttpStatus.OK, counter - 1));
        blockedWebRequestDueToRateLimit(mockMvc, TEST_URL);
    }

    @Test
    @DirtiesContext
    void no_rate_limit_for_user_admin_in_header() throws Exception {
        for(int i = 1; i <=20; i++) {
            mockMvc
                    .perform(get(TEST_URL)
                            .header("user", "admin")
                    )
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andExpect(header().doesNotExist("X-Rate-Limit-Remaining"))
                    .andExpect(content().string(containsString("Hello World")));
        }
    }

    @Test
    @DirtiesContext
    void rate_limit_for_user_bilbo_in_header() throws Exception {
        for(int remainingTries = 5; remainingTries >= 1; remainingTries--) {
            mockMvc
                    .perform(get(TEST_URL)
                            .header(HEADER_USER, "bilbo")
                    )
                    .andExpect(status().is(HttpStatus.OK.value()))
                    .andExpect(header().longValue("X-Rate-Limit-Remaining", remainingTries -1))
                    .andExpect(content().string(containsString("Hello World")));
        }
        blockedWebRequestDueToRateLimit(mockMvc, TEST_URL);
    }

}
