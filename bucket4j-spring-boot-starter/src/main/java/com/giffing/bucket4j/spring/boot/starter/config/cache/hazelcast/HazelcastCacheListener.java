package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryUpdatedListener;

import java.util.ArrayList;
import java.util.List;

public class HazelcastCacheListener<K, V> implements CacheListener<K, V>, EntryUpdatedListener<K, V> {

	private final List<CacheUpdateListener<K, V>> cacheUpdateListeners = new ArrayList<>();

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		cacheUpdateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		cacheUpdateListeners.remove(listener);
	}

	@Override
	public void entryUpdated(EntryEvent<K, V> entryEvent) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(entryEvent.getKey(), entryEvent.getOldValue(), entryEvent.getValue());
		cacheUpdateListeners.forEach(x -> x.onCacheUpdateEvent(updateEvent));
	}

}
