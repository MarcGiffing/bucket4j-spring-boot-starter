package com.giffing.bucket4j.spring.boot.starter.context;

import java.lang.annotation.*;

@Target(value = {ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RateLimiting {

    /**
     * @return The name of the rate limit configuration as a reference to the property file
     */
    String name();

    /**
     * The cache key which is mayby modified the e.g. the method name {@link RateLimiting#ratePerMethod()}
     *
     * @return the cache key.
     */
    String cacheKey() default "";

    /**
     * An optional execute condition which overrides the execute condition from the property file
     *
     * @return the expression in the Spring Expression Language format.
     */
    String executeCondition() default "";

    /**
     * An optional execute condition which overrides the execute condition from the property file
     *
     * @return the expression in the Spring Expression Language format.
     */
    String skipCondition() default "";

    /**
     * The Name of the annotated method will be added to the cache key.
     * It's maybe a problem
     *
     * @return true if the method name should be added to the cache key.
     */
    boolean ratePerMethod() default false;

    /**
     * An optional fall back method which is executed when the rate limit occurs instead of throwing
     * the {@link RateLimitException}.
     * <p>
     * The arguments and the return type of the fallback method must have the same signature.
     *
     * @return the name of the public method which resists in the same class.
     */
    String fallbackMethodName() default "";
}
