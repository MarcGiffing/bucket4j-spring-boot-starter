package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This class is intended to be used as bean.
 * <p>
 * It will listen to Redisson events on the {cacheName}:update channel
 * and publish these to the Spring ApplicationEventPublisher as  {@code CacheUpdateEvent<K, V>}
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
public class RedissonCacheListener<K, V> {

    private ApplicationEventPublisher eventPublisher;

    public RedissonCacheListener(RedissonClient redisson, String cacheName, ApplicationEventPublisher eventPublisher) {
        RTopic pubSubTopic = redisson.getTopic(cacheName);
        pubSubTopic.addListener(CacheUpdateEvent.class, this::onCacheUpdateEvent);
        this.eventPublisher = eventPublisher;
    }

    public void onCacheUpdateEvent(CharSequence channel, CacheUpdateEvent<K, V> event) {
        eventPublisher.publishEvent(event);
    }
}
