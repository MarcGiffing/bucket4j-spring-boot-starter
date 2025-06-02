package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(properties = {
		"bucket4j.enabled=false",
		"bucket4j.filters[0].cache-name=buckets",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
		"bucket4j.filters[0].rate-limits[0].bandwidths[0].refill-speed=interval",
		"bucket4j.filters[0].url-pattern=^(/hello).*",
})
@AutoConfigureMockMvc
@DirtiesContext
public class Bucket4jDisabledTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void helloTest() {
		String url = "/hello";
		IntStream.rangeClosed(1, 50)
			.boxed()
			.sorted(Collections.reverseOrder())
			.forEach(counter -> {
                try {
                    mockMvc
                            .perform(get(url))
                            .andExpect(status().isOk())
                            .andExpect(header().doesNotExist("X-Rate-Limit-Remaining"))
                            .andExpect(content().string(containsString("Hello World")));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
	}


}
