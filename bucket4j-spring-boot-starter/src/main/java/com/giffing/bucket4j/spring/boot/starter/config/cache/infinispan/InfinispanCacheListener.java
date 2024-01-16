package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This class is intended to be used as bean.
 *
 * It will listen to changes in the cache, parse them to a  {@code CacheUpdateEvent<K, V>}
 * and publish the event to the Spring ApplicationEventPublisher.
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
@Listener
public class InfinispanCacheListener<K, V> {

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	public InfinispanCacheListener(Cache<K, V> cache){
		cache.addListener(this);
	}
	@CacheEntryModified
	public void entryModified(CacheEntryModifiedEvent<K, V> event) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(event.getKey(),event.getOldValue(), event.getNewValue());
		this.eventPublisher.publishEvent(updateEvent);
	}
}
