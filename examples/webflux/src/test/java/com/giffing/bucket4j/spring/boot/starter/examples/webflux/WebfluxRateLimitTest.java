package com.giffing.bucket4j.spring.boot.starter.examples.webflux;

import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@ActiveProfiles("webflux") // Like this
@TestPropertySource(properties = {"bucket4j.filter-config-caching-enabled=true"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class WebfluxRateLimitTest {

	@Autowired
    ApplicationContext context;

	@Autowired
	Bucket4JBootProperties properties;

	private final ObjectMapper objectMapper = new ObjectMapper();
    WebTestClient rest;

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
	@Order(2)
	void replaceConfigTest() throws Exception {
		int newFilterCapacity = 1000;

		//get the /hello filter
		Bucket4JConfiguration filter = properties.getFilters().stream().filter(x -> "/hello".matches(x.getUrl())).findFirst().orElse(null);
		assert filter != null;

		//clone the filter so we don't modify the original, increase the version and set the new capacity for all bandwidths
		Bucket4JConfiguration clone = objectMapper.readValue(objectMapper.writeValueAsString(filter),Bucket4JConfiguration.class);
		clone.setMajorVersion(clone.getMajorVersion() + 1);
		clone.getRateLimits().forEach(rl -> {
			rl.getBandwidths().forEach(bw -> bw.setCapacity(newFilterCapacity));
		});

		//update the filter cache
		String url = "/filters/".concat(clone.getId());
		rest.post().uri(url).bodyValue(clone).exchange().expectStatus().isOk();

		//Short sleep to allow the cacheUpdateListeners to update the filter configuration
		Thread.sleep(100);

		//validate that the new capacity is applied to requests
		successfulWebRequest("/hello", newFilterCapacity-1);
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
