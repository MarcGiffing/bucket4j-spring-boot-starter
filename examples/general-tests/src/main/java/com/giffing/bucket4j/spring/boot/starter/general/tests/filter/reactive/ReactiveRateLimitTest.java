package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.reactive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(properties = {
        "bucket4j.enabled=true",
        "bucket4j.filter-config-cache-name=filterConfigCache",
        "bucket4j.filter-config-caching-enabled=true",
        "bucket4j.filters[0].cache-name=buckets",
        "bucket4j.filters[0].id=filter1",
        "bucket4j.filters[0].filter-method=webflux",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=5",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[0].url-pattern=^(/hello).*",
        "bucket4j.filters[1].cache-name=buckets",
        "bucket4j.filters[1].id=filter2",
        "bucket4j.filters[1].filter-method=webflux",
        "bucket4j.filters[1].rate-limits[0].bandwidths[0].capacity=10",
        "bucket4j.filters[1].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[1].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[1].url-pattern=^(/world).*",
        "bucket4j.filters[2].cache-name=buckets",
        "bucket4j.filters[2].id=filter3",
        "bucket4j.filters[2].filter-method=webflux",
        "bucket4j.filters[2].rate-limits[0].bandwidths[0].capacity=5",
        "bucket4j.filters[2].rate-limits[0].bandwidths[0].time=10",
        "bucket4j.filters[2].rate-limits[0].bandwidths[0].unit=seconds",
        "bucket4j.filters[2].rate-limits[0].post-execute-condition=getStatus() eq 401",
        "bucket4j.filters[2].url-pattern=^(/secure).*"
})
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext
public class ReactiveRateLimitTest {

    private static final String NONEXISTENT_FILTER_ID = "nonexistent";

    private static final String FILTER_ID = "filter1";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ApplicationContext context;

    @Autowired
    Bucket4JBootProperties properties;

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
    void helloTest() {
        String url = "/hello";
        IntStream.rangeClosed(1, 5)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach(counter -> successfulWebRequest(url, counter - 1));

        blockedWebRequestDueToRateLimit(url);
    }

    @Test
    @Order(1)
    void worldTest() {
        String url = "/world";
        IntStream.rangeClosed(1, 10)
                .boxed()
                .sorted(Collections.reverseOrder())
                .forEach(counter -> successfulWebRequest(url, counter - 1));

        blockedWebRequestDueToRateLimit(url);
    }

    @Test
    @Order(1)
    void invalidNonMatchingIdReplaceConfigTest() throws Exception {
        Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
        updateFilterCache(NONEXISTENT_FILTER_ID, objectMapper.writeValueAsString(filter))
                .expectStatus().isBadRequest()
                .expectBody().jsonPath("$").value(
                        containsString("The id in the path does not match the id in the request body.")
                );
    }

    @Test
    @Order(1)
    void invalidNonExistingReplaceConfigTest() throws Exception {
        Bucket4JConfiguration filter = getFilterConfigClone(FILTER_ID);
        filter.setId(NONEXISTENT_FILTER_ID);
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
        filter.setFilterMethod(FilterMethod.GATEWAY);
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
        filter.setCacheName(NONEXISTENT_FILTER_ID);
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

        // Allow the cacheUpdateListeners to update the filter configuration
        await().atMost(Duration.ofSeconds(1))
                .untilAsserted(() -> successfulWebRequest(url, newFilterCapacity - 1));
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

    private WebTestClient.ResponseSpec updateFilterCache(String filterId, String content) {
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

    private void blockedWebRequestDueToRateLimit(String url) {
        rest
                .get()
                .uri(url)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.TOO_MANY_REQUESTS)
                .expectBody().jsonPath("error", "Too many requests!");
    }

}