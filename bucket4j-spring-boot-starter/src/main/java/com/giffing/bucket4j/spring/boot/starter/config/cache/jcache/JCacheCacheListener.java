package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;
import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import java.io.Serializable;

/**
 * This class is intended to be used as bean.
 *
 * It will listen to changes in the cache, parse them to a CacheUpdateEvent<K, V>
 * and publish the event to the Spring ApplicationEventPublisher.
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
public class JCacheCacheListener<K, V> implements CacheEntryUpdatedListener<K, V>, Serializable {

	@Autowired
	private ApplicationEventPublisher eventPublisher;

	public JCacheCacheListener(Cache<K,V> cache ){
		cache.registerCacheEntryListener(
				new MutableCacheEntryListenerConfiguration<K, V>
						(FactoryBuilder.factoryOf(this), null, true, false));
	}

	@Override
	public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
		iterable.forEach(event -> {
			CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(event.getKey(), event.getOldValue(), event.getValue());
			eventPublisher.publishEvent(updateEvent);
		});
	}
}
