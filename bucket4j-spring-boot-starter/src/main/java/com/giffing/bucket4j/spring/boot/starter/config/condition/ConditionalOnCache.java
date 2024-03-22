package com.giffing.bucket4j.spring.boot.starter.config.condition;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional @Conditional} that matches under the following conditions.
 * <ul>
 *     <li>The 'bucket4j.cache-to-use' is not set.</li>
 *     <li>The 'bucket4j.cache-to-use' property matches the given {@link #value()}.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "cache-to-use", matchIfMissing = true)
public @interface ConditionalOnCache {

    @AliasFor(annotation = ConditionalOnProperty.class, attribute = "havingValue")
    String value() default "";
}
