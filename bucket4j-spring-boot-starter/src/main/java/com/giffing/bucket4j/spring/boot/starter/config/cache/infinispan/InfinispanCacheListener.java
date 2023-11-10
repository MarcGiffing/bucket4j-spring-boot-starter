package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

@Listener
public class InfinispanCacheListener<K, V> extends CacheListener<K, V> {

	@CacheEntryModified
	public void entryModified(CacheEntryModifiedEvent<K, V> event) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(event.getKey(),event.getOldValue(), event.getNewValue());
		super.onCacheUpdateEvent(updateEvent);
	}
}
