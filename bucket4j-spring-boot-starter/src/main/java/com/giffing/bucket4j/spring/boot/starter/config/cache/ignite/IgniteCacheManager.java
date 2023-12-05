package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;


import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;

import org.apache.ignite.IgniteCache;

public class IgniteCacheManager<K,V> extends CacheManager<K, V> {

	private final IgniteCache<K,V> cache;

	public IgniteCacheManager(IgniteCache<K,V> cache){
		super(new IgniteCacheListener<>());
		this.cache = cache;

		IgniteCacheListener<K, V> cacheListener = (IgniteCacheListener<K, V>) super.cacheListener;
		cache.registerCacheEntryListener(
				new MutableCacheEntryListenerConfiguration<>
						(FactoryBuilder.factoryOf(cacheListener), null, false, false));
	}
	@Override
	public V getValue(K key) {
		return cache.get(key);
	}

	@Override
	public void setValue(K key, V value) {
		cache.put(key, value);
	}
}
