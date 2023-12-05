package com.giffing.bucket4j.spring.boot.starter.examples.gateway;

import java.util.Collections;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {
		"httpbin=http://localhost:${wiremock.server.port}",
		})
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {"bucket4j.filter-config-caching-enabled=true", "bucket4j.filter-config-cache-name=filterConfigCache"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GatewaySampleApplicationTest {

	private final String url = "/hello";

	@Autowired
    ApplicationContext context;

	@Autowired
	Bucket4JBootProperties properties;
	
	WebTestClient rest;

	private final ObjectMapper objectMapper = new ObjectMapper();
    
    @BeforeEach
    public void setup() {
    	this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            .configureClient()
            .build();
    	
    	WireMock.stubFor(WireMock.get(WireMock.urlEqualTo(url))
		        .willReturn(WireMock.aResponse()
		          .withBody("{\"headers\":{\"Hello\":\"World\"}}")
		          .withHeader("Content-Type", "application/json")));
    }

	@Test
	@Order(1)
	void helloTest() throws Exception {
		IntStream.rangeClosed(1, 5)
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
		int newFilterCapacity = 1000;

		//get the /world filter
		Bucket4JConfiguration filter = properties.getFilters().stream().filter(x -> url.matches(x.getUrl())).findFirst().orElse(null);
		assert filter != null;

		//update the first (and only) bandwidth capacity of the first (and only) rate limit of the Filter configuration
		Bucket4JConfiguration clone = objectMapper.readValue(objectMapper.writeValueAsString(filter),Bucket4JConfiguration.class);
		clone.setMajorVersion(clone.getMajorVersion() + 1);
		clone.getRateLimits().get(0).getBandwidths().get(0).setCapacity(newFilterCapacity);

		//update the filter cache
		rest.post()
				.uri("/filters/".concat(clone.getId()))
				.bodyValue(clone)
				.exchange()
				.expectStatus().isOk();

		//Short sleep to allow the cacheUpdateListeners to update the filter configuration
		Thread.sleep(100);

		//validate that the new capacity is applied to requests
		successfulWebRequest(url, newFilterCapacity-1);
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
