package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.IMap;
import com.hazelcast.map.listener.EntryUpdatedListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This class is intended to be used as bean.
 * <p>
 * It will listen to changes in the cache, parse them to a CacheUpdateEvent<K, V>
 * and publish the event to the Spring ApplicationEventPublisher.
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
public class HazelcastCacheListener<K, V> implements EntryUpdatedListener<K, V> {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public HazelcastCacheListener(IMap<K, V> map) {
        map.addEntryListener(this, true);
    }

    @Override
    public void entryUpdated(EntryEvent<K, V> entryEvent) {
        CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(entryEvent.getKey(), entryEvent.getOldValue(), entryEvent.getValue());
        eventPublisher.publishEvent(updateEvent);
    }

}
