package com.giffing.bucket4j.spring.boot.starter.examples.redis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;

import java.util.Collections;
import java.util.stream.IntStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = {"bucket4j.filter-config-caching-enabled=true", "bucket4j.filter-config-cache-name=filterConfigCache"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RedisRedissonTest {

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
    ApplicationContext context;

	@Autowired
	Bucket4JBootProperties properties;

    WebTestClient rest;

	private final ObjectMapper objectMapper = new ObjectMapper();

	private final String FILTER_ID = "filter1";

    @BeforeEach
    public void setup() {
        this.rest = WebTestClient
                .bindToApplicationContext(this.context)
                .configureClient()
                .build();
    }

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
                    System.out.println(counter);
                    successfulWebRequest(url, counter - 1);
                });

        blockedWebRequestDueToRateLimit(url);
    }

	@Test
	@Order(1)
	void invalidNonMatchingIdReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		updateFilterCache("nonexistent", objectMapper.writeValueAsString(filter))
			.expectStatus().isBadRequest()
			.expectBody().jsonPath("$").value(
				containsString("The id in the path does not match the id in the request body.")
			);
	}

	@Test
	@Order(1)
	void invalidNonExistingReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setId("nonexistent");
		updateFilterCache(filter)
			.expectStatus().isNotFound()
			.expectBody().jsonPath("$").value(
				containsString("No filter with id 'nonexistent' could be found.")
			);
	}

	@Test
	@Order(1)
	void invalidVersionReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		updateFilterCache(filter)
			.expectStatus().isBadRequest()
			.expectBody().jsonPath("$").value(
				containsString("The new configuration should have a higher version than the current configuration.")
			);
	}

	@Test
	@Order(1)
	void invalidMethodReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		filter.setFilterMethod(FilterMethod.SERVLET);
		updateFilterCache(filter)
			.expectStatus().isBadRequest()
			.expectBody().jsonPath("$").value(
				containsString("It is not possible to modify the filterMethod of an existing filter.")
			);
	}

	@Test
	@Order(1)
	void invalidOrderReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		filter.setFilterOrder(filter.getFilterOrder() + 1);
		updateFilterCache(filter)
			.expectStatus().isBadRequest()
			.expectBody().jsonPath("$").value(
				containsString("It is not possible to modify the filterOrder of an existing filter.")
			);
	}

	@Test
	@Order(1)
	void invalidCacheNameReplaceConfigTest() throws Exception {
		Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
		filter.setMinorVersion(filter.getMinorVersion() + 1);
		filter.setCacheName("nonexistent");
		updateFilterCache(filter)
			.expectStatus().isBadRequest()
			.expectBody().jsonPath("$").value(
				containsString("It is not possible to modify the cacheName of an existing filter.")
			);
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
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.message").isEqualTo("Configuration validation failed")
			.jsonPath("$.errors.length()").isEqualTo(1)
			.jsonPath("$.errors[0]").isEqualTo("Invalid predicate name: INVALID-EXEC");
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
			.expectStatus().isBadRequest()
			.expectBody()
			.jsonPath("$.message").isEqualTo("Configuration validation failed")
			.jsonPath("$.errors[0]").isEqualTo("Invalid predicate names: INVALID-EXEC, INVALID-SKIP");
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
			.expectStatus().isOk();

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

	private WebTestClient.ResponseSpec updateFilterCache(Bucket4JConfiguration filter) throws Exception {
		return updateFilterCache(filter.getId(), objectMapper.writeValueAsString(filter));
	}

	private WebTestClient.ResponseSpec updateFilterCache(String filterId, String content) throws Exception {
		return rest.post()
			.uri("/filters/".concat(filterId))
			.contentType(MediaType.APPLICATION_JSON)
			.bodyValue(content)
			.exchange();
	}

    private void successfulWebRequest(String url, Integer remainingTries) {
        rest
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Rate-Limit-Remaining", String.valueOf(remainingTries));
    }

    private void blockedWebRequestDueToRateLimit(String url) throws Exception {
        rest
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody().jsonPath("error", "Too many requests!");
    }

}
