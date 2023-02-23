package com.giffing.bucket4j.spring.boot.starter.examples.gateway;

import java.util.Collections;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.github.tomakehurst.wiremock.client.WireMock;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = {
		"httpbin=http://localhost:${wiremock.server.port}",
		})
@AutoConfigureWireMock(port = 0)
class GatewaySampleApplicationTest {

	@Autowired
    ApplicationContext context;
	
	WebTestClient rest;
    
    @BeforeEach
    public void setup() {
    	this.rest = WebTestClient
            .bindToApplicationContext(this.context)
            .configureClient()
            .build();
    	
    	WireMock.stubFor(WireMock.get(WireMock.urlEqualTo("/hello"))
		        .willReturn(WireMock.aResponse()
		          .withBody("{\"headers\":{\"Hello\":\"World\"}}")
		          .withHeader("Content-Type", "application/json")));
    }

	@Test
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
