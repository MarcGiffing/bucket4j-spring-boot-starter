package com.giffing.bucket4j.spring.boot.starter.config.cache;

public interface CacheUpdateListener<K,V> {
	void onCacheUpdateEvent(CacheUpdateEvent<K,V> event);
}
