package com.giffing.bucket4j.spring.boot.starter.examples.caffeine;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimitException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class RateLimitExceptionHandler {

    @ExceptionHandler(value = {RateLimitException.class})
    protected ResponseEntity<Object> handleRateLimit(RateLimitException e) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
    }

}
