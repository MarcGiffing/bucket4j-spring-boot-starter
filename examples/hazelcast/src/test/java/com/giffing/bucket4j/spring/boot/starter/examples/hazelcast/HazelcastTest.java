package com.giffing.bucket4j.spring.boot.starter.examples.hazelcast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {"bucket4j.filter-config-caching-enabled=true", "bucket4j.filter-config-cache-name=filterConfigCache"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HazelcastTest {

    @Autowired
    private MockMvc mockMvc;

	@Autowired
	Bucket4JBootProperties properties;

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String FILTER_ID = "filter1";

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
	void invalidNonMatchingIdReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		updateFilterCache("nonexistent", objectMapper.writeValueAsString(filter))
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("The id in the path does not match the id in the request body.")));
	}

	@Test
	@Order(1)
	void invalidNonExistingReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setId("nonexistent");
		updateFilterCache(filter)
			.andExpect(status().isNotFound())
			.andExpect(content().string(containsString("No filter with id 'nonexistent' could be found.")));
	}

	@Test
	@Order(1)
	void invalidVersionReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		updateFilterCache(filter)
			.andExpect(status().isBadRequest())
			.andExpect(content().string("The new configuration should have a higher version than the current configuration."));
	}

	@Test
	@Order(1)
	void invalidMethodReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		filter.setFilterMethod(FilterMethod.WEBFLUX);
		updateFilterCache(filter)
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("It is not possible to modify the filterMethod of an existing filter.")));
	}

	@Test
	@Order(1)
	void invalidOrderReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		filter.setFilterOrder(filter.getFilterOrder() + 1);
		updateFilterCache(filter)
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("It is not possible to modify the filterOrder of an existing filter.")));
	}

	@Test
	@Order(1)
	void invalidCacheNameReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		filter.setCacheName("nonexistent");
		updateFilterCache(filter)
			.andExpect(status().isBadRequest())
			.andExpect(content().string(containsString("It is not possible to modify the cacheName of an existing filter.")));
	}

	@Test
	@Order(1)
	void invalidPredicateReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		DocumentContext documentContext = JsonPath.parse(objectMapper.writeValueAsString(filter));
		String json = documentContext
			.add("$.rateLimits[0].executePredicates", "INVALID-EXEC=TEST")
			.jsonString();
		updateFilterCache(filter.getId(), json)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Configuration validation failed"))
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[0]").value("Invalid predicate name: INVALID-EXEC"));
	}

	@Test
	@Order(1)
	void invalidPredicatesReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		DocumentContext documentContext = JsonPath.parse(objectMapper.writeValueAsString(filter));
		String json = documentContext
			.add("$.rateLimits[0].executePredicates", "INVALID-EXEC=TEST")
			.add("$.rateLimits[0].skipPredicates", "INVALID-SKIP=TEST")
			.jsonString();
		updateFilterCache(filter.getId(), json)
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("Configuration validation failed"))
			.andExpect(jsonPath("$.errors.length()").value(1))
			.andExpect(jsonPath("$.errors[0]").value("Invalid predicate names: INVALID-EXEC, INVALID-SKIP"));
	}

	@Test
	@Order(2)
	void replaceConfigTest() throws Exception {
		String url = "/hello";
		int newFilterCapacity = 1000;

		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMajorVersion(filter.getMajorVersion() + 1);
		filter.getRateLimits().forEach(rl -> rl.getBandwidths().forEach(bw -> bw.setCapacity(newFilterCapacity)));

		updateFilterCache(filter)
			.andExpect(status().isOk());

		Thread.sleep(100); //Short sleep to allow the cacheUpdateListeners to update the filter configuration
		successfulWebRequest(url, newFilterCapacity - 1);
	}

	private Bucket4JConfiguration getFilterConfigClone(String id) throws JsonProcessingException {
		Bucket4JConfiguration config = properties.getFilters()
			.stream()
			.filter(x -> id.matches(x.getId())).findFirst().orElse(null);
		assertThat(config).isNotNull();
		//returns a clone to prevent modifying the original in the properties
		return objectMapper.readValue(objectMapper.writeValueAsString(config), Bucket4JConfiguration.class);
	}

	private ResultActions updateFilterCache(Bucket4JConfiguration filter) throws Exception {
		return updateFilterCache(filter.getId(), objectMapper.writeValueAsString(filter));
	}

	private ResultActions updateFilterCache(String filterId, String content) throws Exception {
		return this.mockMvc
			.perform(post("/filters/".concat(filterId))
				.contentType(MediaType.APPLICATION_JSON)
				.content(content));
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
