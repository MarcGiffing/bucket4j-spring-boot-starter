package com.giffing.bucket4j.spring.boot.starter.examples.redis;

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
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Collections;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {"bucket4j.filter-config-caching-enabled=true", "bucket4j.filter-config-cache-name=filterConfigCache"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisTest {

    @Container
    static final GenericContainer redis =
            new GenericContainer(DockerImageName.parse("redis:7"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
    }

    @Autowired
    private MockMvc mockMvc;

	@Autowired
	Bucket4JBootProperties properties;

	private final ObjectMapper objectMapper = new ObjectMapper();

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

	@Test
	@Order(2)
	void replaceConfigTest() throws Exception {
		String filterEndpoint = "/world";
		int newFilterCapacity = 1000;

		//get the /world filter
		Bucket4JConfiguration filter = properties.getFilters().stream().filter(x -> filterEndpoint.matches(x.getUrl())).findFirst().orElse(null);
		assert filter != null;

		//update the first (and only) bandwidth capacity of the first (and only) rate limit of the Filter configuration
		Bucket4JConfiguration clone = objectMapper.readValue(objectMapper.writeValueAsString(filter),Bucket4JConfiguration.class);
		clone.setMajorVersion(clone.getMajorVersion() + 1);
		clone.getRateLimits().get(0).getBandwidths().get(0).setCapacity(newFilterCapacity);

		//update the filter cache
		String url = "/filters/".concat(clone.getId());
		this.mockMvc
				.perform(post(url)
						.contentType(MediaType.APPLICATION_JSON)
						.content(new ObjectMapper().writeValueAsString(clone)))
				.andExpect(status().isOk());

		//Short sleep to allow the cacheUpdateListeners to update the filter configuration
		Thread.sleep(100);

		//validate that the new capacity is applied to requests
		successfulWebRequest(filterEndpoint, newFilterCapacity-1);
	}


	private void blockedWebRequestDueToRateLimit(String url) throws Exception {
        this.mockMvc
                .perform(get(url))
                .andExpect(status().is(HttpStatus.TOO_MANY_REQUESTS.value()))
                .andExpect(content().string(containsString("{ \"message\": \"Too many requests!\" }")));
    }
}
