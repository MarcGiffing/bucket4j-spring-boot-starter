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

import static com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.MockMvcHelper.blockedWebRequestDueToRateLimit;
import static com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.MockMvcHelper.webRequestWithStatus;
import static org.assertj.core.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;


@SpringBootTest(properties = {
		"bucket4j.filters[0].cache-name=buckets",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].refill-speed=interval",
		"bucket4j.filters[0].url=^(/hello).*",

		"bucket4j.filters[1].rate-limits[0].post-execute-condition= getStatus() eq 401",
		"bucket4j.filters[1].rate-limits[0].bandwidths[0].capacity=5",
		"bucket4j.filters[1].rate-limits[0].bandwidths[0].time=10",
		"bucket4j.filters[1].rate-limits[0].bandwidths[0].unit=seconds",
		"bucket4j.filters[1].rate-limits[0].bandwidths[0].refill-speed=interval",
		"bucket4j.filters[1].url=^(/secure).*",
})
@AutoConfigureMockMvc
@DirtiesContext
public class PostExecuteConditionTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void assert_rate_limit_when_unauthorized() throws Exception {
		String url = "/secure";
		IntStream.rangeClosed(1, 5)
				.boxed()
				.sorted(Collections.reverseOrder())
				.forEach(counter -> webRequestWithStatus(mockMvc, url, counter, HttpStatus.UNAUTHORIZED));

		blockedWebRequestDueToRateLimit(mockMvc, url);
	}

	@Test
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
						e.printStackTrace();
						fail(e.getMessage());
					}
				});
	}

}
