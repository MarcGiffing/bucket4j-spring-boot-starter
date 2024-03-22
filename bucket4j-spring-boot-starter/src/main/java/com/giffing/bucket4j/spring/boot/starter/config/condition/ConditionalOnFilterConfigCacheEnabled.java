package com.giffing.bucket4j.spring.boot.starter.config.condition;

import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link Conditional @Conditional} that matches under the following conditions.
 * <ul>
 *     <li>The 'bucket4j.filter-config-caching-enabled' is set to true}.</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, name = "filter-config-caching-enabled", havingValue = "true")
public @interface ConditionalOnFilterConfigCacheEnabled {
}
