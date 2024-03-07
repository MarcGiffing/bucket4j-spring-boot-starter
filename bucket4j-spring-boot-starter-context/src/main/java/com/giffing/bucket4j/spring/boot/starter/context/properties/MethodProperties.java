package com.giffing.bucket4j.spring.boot.starter.context.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MethodProperties {

    @NotBlank
    private String name;

    @NotBlank
    private String cacheName;

    @NotNull
    private RateLimit rateLimit;

}