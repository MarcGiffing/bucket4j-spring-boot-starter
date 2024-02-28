package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class MockMvcHelper {

    public static void webRequestWithStatus(
            MockMvc mockMvc,
            String url,
            HttpStatus httpStatus,
            Integer remainingTries) {
        try {
            mockMvc
                    .perform(get(url))
                    .andExpect(status().is(httpStatus.value()))
                    .andExpect(header().longValue("X-Rate-Limit-Remaining", remainingTries))
                    .andExpect(content().string(containsString("Hello World")));
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    public static void blockedWebRequestDueToRateLimit(MockMvc mockMvc, String url) throws Exception {
        mockMvc
                .perform(get(url))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
                .andExpect(content().string(containsString("{ \"message\": \"Too many requests!\" }")));
    }

    public static void blockedWebRequestDueToRateLimitWithEmptyBody(MockMvc mockMvc, String url) throws Exception {
        mockMvc
                .perform(get(url))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
                .andExpect(jsonPath("$").doesNotExist());
    }
}
