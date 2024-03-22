package com.giffing.bucket4j.spring.boot.starter.config.condition;

import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * {@link Conditional @Conditional} that only matches when in the Bucket4j properties
 * a synchronous configuration exists. E.g. the is a method configuration for the @{@link RateLimiting} annotation
 * or a Servlet Filter configuration exists.
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(OnSynchronousPropertyCondition.class)
public @interface ConditionalOnSynchronousPropertyCondition {
}
