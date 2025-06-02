package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds all the relevant starter properties which can be configured with
 * Spring Boots application.properties / application.yml configuration files.
 */
@Data
@ConfigurationProperties(prefix = Bucket4JBootProperties.PROPERTY_PREFIX)
@Validated
public class Bucket4JBootProperties {

    public static final String PROPERTY_PREFIX = "bucket4j";

    /**
     * Enables or disables the Bucket4j Spring Boot Starter.
     */
    @NotNull
    private Boolean enabled = true;

    /**
     * Sets the cache implementation which should be auto configured.
     * This property can be used if multiple caches are configured by the starter
     * and you have to choose one due to a startup error.
     * <ul>
     * 	<li>jcache</li>
     *  <li>hazelcast</li>
     *  <li>ignite</li>
     *  <li>redis</li>
     * </ul>
     */
    private String cacheToUse;

    /**
     * Defines which type of URL pattern parser will be used to match incoming request paths and queries.
     *
     * <p>Available options:</p>
     * <ul>
     *     <li><b>regex</b> — uses regular Java regular expressions (e.g., <code>^/api/.*</code>)</li>
     *     <li><b>path-pattern</b> — uses Spring-style path patterns (e.g., <code>/api/**</code>)</li>
     * </ul>
     *
     * <p>Default is <b>regex</b>.</p>
     * <p>This setting controls how URL patterns in Bucket4j filters will be interpreted.</p>
     */
    private String urlPatternParser = "regex";

    /**
     * Configuration for the {@link RateLimiting} annotation on method level.
     */
    @Valid
    private List<MethodProperties> methods = new ArrayList<>();

    private boolean filterConfigCachingEnabled = false;

    /**
     * If Filter configuration caching is enabled, a cache with this name should exist, or it will cause an exception.
     */
    @NotBlank
    private String filterConfigCacheName = "filterConfigCache";


    @Valid
    private List<Bucket4JConfiguration> filters = new ArrayList<>();

    @AssertTrue(message = "FilterConfiguration caching is enabled, but not all filters have an identifier configured")
    public boolean isValidFilterIds() {
        return !filterConfigCachingEnabled || filters.stream().noneMatch(filter -> filter.getId() == null);
    }

    /**
     * A list of default metric tags which should be applied to all filters
     */
    @Valid
    private List<MetricTag> defaultMetricTags = new ArrayList<>();

    /**
     * A list of default metric tags which should be applied to all methods.
     * Additional configuration is necessary as the evaluation context for resolving
     * tag expression is different from filters.
     */
    @Valid
    private List<MetricTag> defaultMethodMetricTags = new ArrayList<>();

    @NotBlank
    private String defaultHttpContentType = "application/json";

    @NotNull
    private HttpStatus defaultHttpStatusCode = HttpStatus.TOO_MANY_REQUESTS;

    /**
     * The HTTP content which should be used in case of rate limiting
     */
    private String defaultHttpResponseBody = "{ \"message\": \"Too many requests!\" }";


    public static String getPropertyPrefix() {
        return PROPERTY_PREFIX;
    }

}
