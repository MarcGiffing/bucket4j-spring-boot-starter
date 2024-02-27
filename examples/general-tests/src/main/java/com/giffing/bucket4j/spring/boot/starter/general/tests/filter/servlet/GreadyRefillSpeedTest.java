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


@SpringBootTest(properties = {
		"bucket4j.filters[0].cache-name=buckets",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].refill-speed=greedy",
		"bucket4j.filters[0].url=^(/hello).*",
})
@AutoConfigureMockMvc
@DirtiesContext
public class GreadyRefillSpeedTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void helloTest() throws Exception {
		String url = "/hello";
		IntStream.rangeClosed(1, 5)
			.boxed()
			.sorted(Collections.reverseOrder())
			.forEach(counter -> webRequestWithStatus(mockMvc, url, counter - 1, HttpStatus.OK));
		blockedWebRequestDueToRateLimit(mockMvc, url);
	}


}
