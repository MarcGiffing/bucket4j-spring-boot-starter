package com.giffing.bucket4j.spring.boot.starter.test.aop;

import com.giffing.bucket4j.spring.boot.starter.Bucket4jStartupCheckConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.aspect.Bucket4jAopConfig;
import com.giffing.bucket4j.spring.boot.starter.config.aspect.RateLimitAspect;
import com.giffing.bucket4j.spring.boot.starter.config.cache.jcache.JCacheBucket4jConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.service.ServiceConfiguration;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ExtendWith(SpringExtension.class)
@EnableCaching
@EnableAspectJAutoProxy
@Import({
        CacheAutoConfiguration.class,
        Bucket4jAopConfig.class,
        Bucket4jStartupCheckConfiguration.class,
        ServiceConfiguration.class,
        JCacheBucket4jConfiguration.class,
        Bucket4JAnnotationTestAutoconfiguration.class,
        RateLimitAspect.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public @interface Bucket4JAnnotationTest {
}
