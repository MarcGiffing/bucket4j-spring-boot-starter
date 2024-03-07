package com.giffing.bucket4j.spring.boot.starter.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiting {

    String name();

    String cacheKey() default "";

    String executeCondition() default "";

    String skipCondition() default "";

    String fallbackMethodName() default "";
}
