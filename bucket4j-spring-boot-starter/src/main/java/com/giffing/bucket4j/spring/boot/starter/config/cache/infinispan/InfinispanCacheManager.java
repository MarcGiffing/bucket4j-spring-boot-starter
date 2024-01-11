package com.giffing.bucket4j.spring.boot.starter.config.cache.infinispan;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import org.infinispan.Cache;

public class InfinispanCacheManager<K, V> implements CacheManager<K, V> {

	private final Cache<K, V> cache;

	public InfinispanCacheManager(Cache<K, V> cache) {
		this.cache = cache;
	}
	@Override
	public V getValue(K key) {
		return cache.get(key);
	}

	@Override
	public void setValue(K key, V value) {
		this.cache.put(key, value);
	}
}
