package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class IgniteCacheListener<K,V> implements CacheListener<K,V>, CacheEntryUpdatedListener<K,V>, Serializable {

	private final List<CacheUpdateListener<K,V>> updateListeners = new ArrayList<>();

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.updateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.updateListeners.remove(listener);
	}

	@Override
	public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
		iterable.forEach(event -> {
			CacheUpdateEvent<K,V> updateEvent = new CacheUpdateEvent<>(event.getKey(), event.getOldValue(), event.getValue());
			updateListeners.forEach(x -> x.onCacheUpdateEvent(updateEvent));
		});
	}
}