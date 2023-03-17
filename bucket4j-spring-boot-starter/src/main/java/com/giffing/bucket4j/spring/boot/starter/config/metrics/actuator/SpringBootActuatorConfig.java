package com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;


@Configuration
@ConditionalOnClass(value = { Bucket4jEndpoint.class})
@Import( value = {Bucket4jEndpoint.class, Bucket4jMetricsConfiguration.class})
public class SpringBootActuatorConfig {

}
