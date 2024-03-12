package com.giffing.bucket4j.spring.boot.starter.config.aspect;

import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.config.service.ServiceConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Enables the support for the {@link RateLimiting} annotation to rate limit on method level.
 */
@Configuration
@ConditionalOnClass(Aspect.class)
@ConditionalOnProperty(prefix = Bucket4JBootProperties.PROPERTY_PREFIX, value = {"enabled"}, matchIfMissing = true)
@EnableConfigurationProperties({Bucket4JBootProperties.class})
@AutoConfigureAfter(value = { CacheAutoConfiguration.class, Bucket4jCacheConfiguration.class })
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {ServiceConfiguration.class, Bucket4jCacheConfiguration.class, SpringBootActuatorConfig.class})
public class Bucket4jAopConfig {

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService, Bucket4JBootProperties bucket4JBootProperties, SyncCacheResolver syncCacheResolver) {
        return new RateLimitAspect(rateLimitService, bucket4JBootProperties.getMethods(), syncCacheResolver);
    }

}
