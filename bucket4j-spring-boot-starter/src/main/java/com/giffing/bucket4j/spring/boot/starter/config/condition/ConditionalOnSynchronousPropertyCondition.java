package com.giffing.bucket4j.spring.boot.starter.config.condition;

import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSynchronousPropertyCondition.class)
public @interface ConditionalOnSynchronousPropertyCondition {
}
