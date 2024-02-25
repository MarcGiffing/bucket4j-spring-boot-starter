package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.apache.ignite.IgniteCache;
import org.springframework.context.ApplicationEventPublisher;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

/**
 * This class is intended to be used as bean.
 *
 * It will listen to changes in the cache, parse them to a  {@code CacheUpdateEvent<K, V>}
 * and publish the event to the Spring ApplicationEventPublisher.
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
public class IgniteCacheListener<K,V> implements CacheEntryUpdatedListener<K,V>, Serializable {

	private ApplicationEventPublisher eventPublisher;

	public IgniteCacheListener(IgniteCache<K,V> cache, ApplicationEventPublisher eventPublisher){
		cache.registerCacheEntryListener(
                new MutableCacheEntryListenerConfiguration<>
                        (FactoryBuilder.factoryOf(this), null, false, false));
		this.eventPublisher = eventPublisher;
	}

	@Override
	public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
		iterable.forEach(event -> {
			CacheUpdateEvent<K,V> updateEvent = new CacheUpdateEvent<>(event.getKey(), event.getOldValue(), event.getValue());
			eventPublisher.publishEvent(updateEvent);
		});
	}
}
