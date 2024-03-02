package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.reactive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Collections;
import java.util.stream.IntStream;

@SpringBootTest(properties = {
        "bucket4j.filters[0].cache-name=buckets",
        "bucket4j.filters[0].filter-method=webflux",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].refill-speed=greedy",
        "bucket4j.filters[0].url=^(/hello).*",
})
@AutoConfigureMockMvc
@DirtiesContext
public class ReactiveGreadyRefillSpeedTest {

    @Autowired
    ApplicationContext context;

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
                .forEach(counter -> successfulWebRequest(url, counter - 1));

        blockedWebRequestDueToRateLimit(url);
    }

    private void blockedWebRequestDueToRateLimit(String url) throws Exception {
        rest
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody().jsonPath("error", "Too many requests!");
    }

    private void successfulWebRequest(String url, Integer remainingTries) {
        rest
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("X-Rate-Limit-Remaining", String.valueOf(remainingTries));
    }

}
