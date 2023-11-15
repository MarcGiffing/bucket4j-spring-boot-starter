package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

import java.util.ArrayList;
import java.util.List;

@Listener
public class InfinispanCacheListener<K, V> implements CacheListener<K, V> {

	private final List<CacheUpdateListener<K,V>> updateListeners = new ArrayList<>();

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		updateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		updateListeners.remove(listener);
	}
	@CacheEntryModified
	public void entryModified(CacheEntryModifiedEvent<K, V> event) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(event.getKey(),event.getOldValue(), event.getNewValue());
		this.updateListeners.forEach(x -> x.onCacheUpdateEvent(updateEvent));
	}

}
