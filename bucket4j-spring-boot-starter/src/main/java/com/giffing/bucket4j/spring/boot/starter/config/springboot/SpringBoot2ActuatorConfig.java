package com.giffing.bucket4j.spring.boot.starter.config.springboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.giffing.bucket4j.spring.boot.starter.springboot2.actuator.Bucket4jEndpoint;
import com.giffing.bucket4j.spring.boot.starter.springboot2.actuator.Bucket4jMetrics;


@Configuration
@ConditionalOnClass(value = { Bucket4jEndpoint.class})
@Import( value = {Bucket4jEndpoint.class, Bucket4jMetrics.class})
public class SpringBoot2ActuatorConfig {

}
