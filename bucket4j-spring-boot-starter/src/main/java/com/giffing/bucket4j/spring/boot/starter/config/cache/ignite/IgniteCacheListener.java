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

public class IgniteCacheListener<K,V> implements CacheEntryUpdatedListener<K,V>, Serializable {

	private final ApplicationEventPublisher eventPublisher;

	public IgniteCacheListener(ApplicationEventPublisher eventPublisher, IgniteCache<K,V> cache){
		this.eventPublisher = eventPublisher;
		cache.registerCacheEntryListener(
				new MutableCacheEntryListenerConfiguration<K, V>
						(FactoryBuilder.factoryOf(this), null, false, false));
	}

	@Override
	public void onUpdated(Iterable<CacheEntryEvent<? extends K, ? extends V>> iterable) throws CacheEntryListenerException {
		iterable.forEach(event -> {
			CacheUpdateEvent<K,V> updateEvent = new CacheUpdateEvent<>(event.getKey(), event.getOldValue(), event.getValue());
			eventPublisher.publishEvent(updateEvent);
		});
	}
}
