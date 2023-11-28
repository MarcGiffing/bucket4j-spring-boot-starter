package com.giffing.bucket4j.spring.boot.starter.config.cache;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

public interface CacheUpdateListener<K,V> {

	@Async
	@EventListener(CacheUpdateEvent.class)
	void onCacheUpdateEvent(CacheUpdateEvent<K,V> event);
}
