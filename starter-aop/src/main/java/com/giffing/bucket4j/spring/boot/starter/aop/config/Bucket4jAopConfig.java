package com.giffing.bucket4j.spring.boot.starter.aop.config;

import com.giffing.bucket4j.spring.boot.starter.aop.aspect.RateLimitAspect;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.actuator.SpringBootActuatorConfig;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.service.ServiceConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.service.RateLimitService;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * Enables the support for the {@link RateLimiting} annotation to rate limit on method level.
 */
@AutoConfiguration
@ConditionalOnBucket4jEnabled
@ConditionalOnClass(Aspect.class)
@EnableConfigurationProperties({Bucket4JBootProperties.class})
@ConditionalOnBean(value = SyncCacheResolver.class)
@Import(value = {ServiceConfiguration.class, SpringBootActuatorConfig.class})
public class Bucket4jAopConfig {

    @Bean
    public RateLimitAspect rateLimitAspect(RateLimitService rateLimitService, Bucket4JBootProperties bucket4JBootProperties, SyncCacheResolver syncCacheResolver, List<MetricHandler> metricHandlers) {
        return new RateLimitAspect(rateLimitService, bucket4JBootProperties, syncCacheResolver, metricHandlers);
    }
}
