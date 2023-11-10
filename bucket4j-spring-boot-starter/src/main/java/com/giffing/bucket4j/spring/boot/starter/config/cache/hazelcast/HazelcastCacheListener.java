package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.listener.EntryUpdatedListener;

public class HazelcastCacheListener<K,V> extends CacheListener<K,V> implements EntryUpdatedListener<K,V> {

	@Override
	public void entryUpdated(EntryEvent<K,V> entryEvent) {
		CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(entryEvent.getKey(), entryEvent.getOldValue(), entryEvent.getValue());
		super.onCacheUpdateEvent(updateEvent);
	}
}
