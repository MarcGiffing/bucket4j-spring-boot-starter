package com.giffing.bucket4j.spring.boot.starter.config.springboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.Bucket4jEndpoint;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.Bucket4jMetricsConfiguration;


@Configuration
@ConditionalOnClass(value = { Bucket4jEndpoint.class})
@Import( value = {Bucket4jEndpoint.class, Bucket4jMetricsConfiguration.class})
public class SpringBootActuatorConfig {

}
