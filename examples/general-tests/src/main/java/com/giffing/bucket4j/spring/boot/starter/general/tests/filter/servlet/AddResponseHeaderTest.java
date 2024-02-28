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

import static com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet.MockMvcHelper.webRequestWithStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest(properties = {
		"bucket4j.filters[0].cache-name=buckets",
		"bucket4j.filters[0].url=.*",
		"bucket4j.filters[0].http-response-headers.hello=world",
		"bucket4j.filters[0].http-response-headers.abc=cba",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
})
@AutoConfigureMockMvc
@DirtiesContext
public class AddResponseHeaderTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void assert_custom_response_header() throws Exception {
		String url = "/hello";
		IntStream.rangeClosed(1, 5)
				.boxed()
				.sorted(Collections.reverseOrder())
				.forEach(counter -> webRequestWithStatus(mockMvc, url, HttpStatus.OK, counter - 1));

		mockMvc
				.perform(get(url))
				.andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
				.andExpect(header().exists("X-Rate-Limit-Retry-After-Seconds"))
				.andExpect(header().string("hello", "world"))
				.andExpect(header().string("abc", "cba"));
	}


}
