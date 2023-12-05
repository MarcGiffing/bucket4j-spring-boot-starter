package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;

import javax.cache.Cache;
import javax.cache.configuration.FactoryBuilder;
import javax.cache.configuration.MutableCacheEntryListenerConfiguration;

public class JCacheCacheManager<K, V> extends CacheManager<K, V> {

	private final Cache<K,V> cache;
	protected JCacheCacheManager(Cache<K, V> cache) {
		super(new JCacheCacheListener<>());
		this.cache = cache;

		JCacheCacheListener<K, V> cacheListener = (JCacheCacheListener<K, V>) super.cacheListener;

		cache.registerCacheEntryListener(
				new MutableCacheEntryListenerConfiguration<>
						(FactoryBuilder.factoryOf(cacheListener), null, true, false));
	}

	@Override
	public V getValue(K key) {
		return this.cache.get(key);
	}

	@Override
	public void setValue(K key, V value) {
		this.cache.put(key, value);
	}
}
