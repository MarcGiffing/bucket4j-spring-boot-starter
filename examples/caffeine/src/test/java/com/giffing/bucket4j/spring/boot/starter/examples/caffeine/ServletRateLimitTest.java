package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("servlet") // Like this
@TestPropertySource(properties = {"bucket4j.filter-config-caching-enabled=true", "bucket4j.filter-config-cache-name=filterConfigCache"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServletRateLimitTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private Bucket4JBootProperties properties;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@Order(1)
	void helloTest() throws Exception {
		String url = "/hello";
		IntStream.rangeClosed(1, 5)
			.boxed()
			.sorted(Collections.reverseOrder())
			.forEach(counter -> {
				successfulWebRequest(url, counter - 1);
			});
		
		blockedWebRequestDueToRateLimit(url);
	}

	
	@Test
	@Order(1)
	void worldTest() throws Exception {
		String url = "/world";
		IntStream.rangeClosed(1, 10)
			.boxed()
			.sorted(Collections.reverseOrder())
			.forEach(counter -> {
				successfulWebRequest(url, counter - 1);
			});
		
		blockedWebRequestDueToRateLimit(url);
	}
	@Test
	@Order(2)
	void replaceConfigTest() throws Exception {
		String url = "/world";
		int newFilterCapacity = 1000;

		//get the /hello filter
		Bucket4JConfiguration filter = properties.getFilters().stream().filter(x -> url.matches(x.getUrl())).findFirst().orElse(null);
		assert filter != null;

		//clone the filter so we don't modify the original, increase the version and set the new capacity for all bandwidths
		Bucket4JConfiguration clone = objectMapper.readValue(objectMapper.writeValueAsString(filter),Bucket4JConfiguration.class);
		clone.setMajorVersion(clone.getMajorVersion() + 1);
		clone.getRateLimits().forEach(rl -> {
			rl.getBandwidths().forEach(bw -> bw.setCapacity(newFilterCapacity));
		});

		//update the filter cache
		this.mockMvc
				.perform(post("/filters/".concat(clone.getId()))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(clone)))
				.andExpect(status().isOk());

		//Short sleep to allow the cacheUpdateListeners to update the filter configuration
		Thread.sleep(100);

		//validate that the new capacity is applied to requests
		successfulWebRequest(url, newFilterCapacity-1);
	}

	private void successfulWebRequest(String url, Integer remainingTries) {
		try {
			this.mockMvc
			.perform(get(url))
			.andExpect(status().isOk())
			.andExpect(header().longValue("X-Rate-Limit-Remaining", remainingTries))
			.andExpect(content().string(containsString("Hello World")));
		} catch (Exception e) {
			e.printStackTrace();
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
