package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.infinispan.Cache;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.springframework.context.ApplicationEventPublisher;

@Listener
public class InfinispanCacheListener<K, V> {

	private final ApplicationEventPublisher eventPublisher;

	public InfinispanCacheListener(ApplicationEventPublisher eventPublisher, Cache<K, V> cache){
		this.eventPublisher = eventPublisher;
		cache.addListener(this);
	}
	@CacheEntryModified
	public void entryModified(CacheEntryModifiedEvent<K, V> event) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(event.getKey(),event.getOldValue(), event.getNewValue());
		this.eventPublisher.publishEvent(updateEvent);
	}
}
