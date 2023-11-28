package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationEventPublisher;

public class RedissonCacheListener<K, V> {

	private final ApplicationEventPublisher eventPublisher;

	public RedissonCacheListener(ApplicationEventPublisher eventPublisher, RedissonClient redisson, String cacheName) {
		this.eventPublisher = eventPublisher;
		RTopic pubSubTopic = redisson.getTopic(cacheName);
		pubSubTopic.addListener(CacheUpdateEvent.class, this::onCacheUpdateEvent);
	}

	public void onCacheUpdateEvent(CharSequence channel, CacheUpdateEvent<K, V> event) {
		eventPublisher.publishEvent(event);
	}
}
