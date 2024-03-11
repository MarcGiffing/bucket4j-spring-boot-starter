package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MethodProperties {

    /**
     * The name of the configuration to reference in the {@link RateLimiting} annotation.
     */
    @NotBlank
    private String name;

    /**
     * The name of the cache.
     */
    @NotBlank
    private String cacheName;

    /**
     * The rate limit configuration
     */
    @NotNull
    private RateLimit rateLimit;

}