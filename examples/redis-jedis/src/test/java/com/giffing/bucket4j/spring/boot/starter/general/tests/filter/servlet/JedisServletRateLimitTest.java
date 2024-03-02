package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.JedisConfiguraiton;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Import(JedisConfiguraiton.class)
public class JedisServletRateLimitTest extends ServletRateLimitTest {

    @Container
    static final GenericContainer redis =
            new GenericContainer(DockerImageName.parse("redis:7"))
                    .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getFirstMappedPort());
    }

}
