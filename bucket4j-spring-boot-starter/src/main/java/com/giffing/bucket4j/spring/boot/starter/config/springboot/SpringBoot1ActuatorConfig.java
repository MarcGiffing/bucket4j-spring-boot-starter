package com.giffing.bucket4j.spring.boot.starter.config.springboot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.giffing.bucket4j.spring.boot.starter.springboot1.actuator.Bucket4jEndpoint;

@Configuration
@ConditionalOnClass(Bucket4jEndpoint.class)
@Import(Bucket4jEndpoint.class)
public class SpringBoot1ActuatorConfig {

}
