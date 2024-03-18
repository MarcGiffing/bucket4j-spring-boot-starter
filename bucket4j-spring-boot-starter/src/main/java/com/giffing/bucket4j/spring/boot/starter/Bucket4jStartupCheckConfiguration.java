package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.context.annotation.Configuration;


@Configuration
@AutoConfigureAfter( value = {
        CacheAutoConfiguration.class,
        Bucket4jCacheConfiguration.class
})
@RequiredArgsConstructor
public class Bucket4jStartupCheckConfiguration {


    @Nullable
    private final SyncCacheResolver syncCacheResolver;

    @Nullable
    private final AsyncCacheResolver asyncCacheResolver;


    @PostConstruct
    public void checkCache() {
        // TODO check filter configuration, check method configuration
        if(syncCacheResolver == null && asyncCacheResolver == null) {
            // TODO throw proper exception
            throw new IllegalStateException("cache not configured properly");
        }
    }

}
