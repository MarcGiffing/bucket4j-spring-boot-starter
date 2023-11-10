package com.giffing.bucket4j.spring.boot.starter.config.cache;

import java.util.ArrayList;
import java.util.List;

public abstract class CacheListener<K,V> {
	private final List<CacheUpdateListener<K,V>> listeners = new ArrayList<>();
	public void addCacheUpdateListener(CacheUpdateListener<K,V> handler) {
		listeners.add(handler);
	}

	public void removeCacheUpdateListener(CacheUpdateListener<K,V> handler){
		listeners.remove(handler);
	}

	public void onCacheUpdateEvent(CacheUpdateEvent<K, V> event) {
		listeners.forEach(x -> x.onCacheUpdateEvent(event));
	}
}
