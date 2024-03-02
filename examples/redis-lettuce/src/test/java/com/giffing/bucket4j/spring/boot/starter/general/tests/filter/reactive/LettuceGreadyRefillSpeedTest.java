package com.giffing.bucket4j.spring.boot.starter.general.tests.filter.reactive;

import com.giffing.bucket4j.spring.boot.starter.LettuceConfiguraiton;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@Import(LettuceConfiguraiton.class)
public class LettuceGreadyRefillSpeedTest extends ReactiveGreadyRefillSpeedTest {

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
