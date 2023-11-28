package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.springframework.context.ApplicationEventPublisher;

public class HazelcastCacheListener<K, V> implements EntryUpdatedListener<K, V> {

	private final ApplicationEventPublisher eventPublisher;

	public HazelcastCacheListener(ApplicationEventPublisher eventPublisher, IMap<K, V> map){
		this.eventPublisher = eventPublisher;
		map.addEntryListener(this, true);
	}

	@Override
	public void entryUpdated(EntryEvent<K, V> entryEvent) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(entryEvent.getKey(), entryEvent.getOldValue(), entryEvent.getValue());
		eventPublisher.publishEvent(updateEvent);
	}

}
