package com.giffing.bucket4j.spring.boot.starter.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * {@link Conditional @Conditional} that only matches when in the Bucket4j properties
 * asynchronous configuration exists. E.g. there are reactive filters registered (WEBFLUX, GATEWAY).
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnAsynchronousPropertyCondition.class)
public @interface ConditionalOnAsynchronousPropertyCondition {
}
