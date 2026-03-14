package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = {
        "bucket4j.enabled=true",
        "bucket4j.filters[0].cache-name=buckets",
        "bucket4j.filters[0].id=filter1",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[0].url=^(/hello).*",
        "bucket4j.filters[1].cache-name=buckets",
        "bucket4j.filters[1].id=filter2",
        "bucket4j.filters[1].rate-limits[0].bandwidths[0].capacity=10",
        "bucket4j.filters[1].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[1].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[1].url=^(/world).*",
        "bucket4j.filters[2].cache-name=buckets",
        "bucket4j.filters[2].id=filter3",
        "bucket4j.filters[2].rate-limits[0].bandwidths[0].capacity=5",
        "bucket4j.filters[2].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[2].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[2].rate-limits[0].post-execute-condition=getStatus() eq 401",
        "bucket4j.filters[2].url=^(/secure).*",
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
@Slf4j
public class ServletRateLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    void helloTest() throws Exception {
        String url = "/hello";
        IntStream.rangeClosed(1, 5)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach(counter -> successfulWebRequest(url, counter - 1, HttpStatus.OK));

        blockedWebRequestDueToRateLimit(url);
    }

    @Test
    @Order(1)
    void assert_rate_limit_when_unauthorized() throws Exception {
        String url = "/secure";
        IntStream.rangeClosed(1, 5)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach(counter -> successfulWebRequest(url, counter, HttpStatus.UNAUTHORIZED));

        blockedWebRequestDueToRateLimit(url);
    }

    @Test
    @Order(1)
    void assert_no_rate_limit_when_authorized() {
        String url = "/secure";
        IntStream.rangeClosed(1, 5)
                .forEach(counter -> {
                    try {
                        this.mockMvc
                                .perform(get(url)
                                        .queryParam("username", "admin")
                                )
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("Hello World")))
                                // the rate limit does not decrease
                                .andExpect(header().string("X-Rate-Limit-Remaining", "5"));

                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        fail(e.getMessage());
                    }
                });
    }


    @Test
    @Order(1)
    void worldTest() throws Exception {
        String url = "/world";
        IntStream.rangeClosed(1, 10)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach(counter -> successfulWebRequest(url, counter - 1, HttpStatus.OK));

        blockedWebRequestDueToRateLimit(url);
    }

    private void successfulWebRequest(String url, Integer remainingTries, HttpStatus httpStatus) {
        try {
            this.mockMvc
                    .perform(get(url))
                    .andExpect(status().is(httpStatus.value()))
                    .andExpect(header().longValue("X-Rate-Limit-Remaining", remainingTries))
                    .andExpect(content().string(containsString("Hello World")));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private void blockedWebRequestDueToRateLimit(String url) throws Exception {
        this.mockMvc
                .perform(get(url))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
                .andExpect(content().string(containsString("{ \"message\": \"Too many requests!\" }")));
    }

}
