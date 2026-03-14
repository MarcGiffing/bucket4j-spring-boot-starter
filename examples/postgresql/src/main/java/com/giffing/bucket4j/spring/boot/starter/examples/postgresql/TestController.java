package com.giffing.bucket4j.spring.boot.starter.examples.postgresql;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<String> hello() {
        return ResponseEntity.ok("Hello from PostgreSQL example!");
    }

    @GetMapping("/world")
    public ResponseEntity<String> world() {
        return ResponseEntity.ok("World from PostgreSQL example!");
    }
}
