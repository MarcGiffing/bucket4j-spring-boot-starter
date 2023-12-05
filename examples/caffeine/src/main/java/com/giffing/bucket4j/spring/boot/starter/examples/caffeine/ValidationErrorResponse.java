package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import java.util.List;

import lombok.Getter;

@Getter
public record ValidationErrorResponse(String message, List<String> errors) {

}
