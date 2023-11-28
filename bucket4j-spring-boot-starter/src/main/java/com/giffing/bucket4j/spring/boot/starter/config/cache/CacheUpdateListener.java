package com.giffing.bucket4j.spring.boot.starter.config.cache;

import org.springframework.context.event.EventListener;

public interface CacheUpdateListener<K,V> {

	@EventListener(CacheUpdateEvent.class)
	void onCacheUpdateEvent(CacheUpdateEvent<K,V> event);
}
