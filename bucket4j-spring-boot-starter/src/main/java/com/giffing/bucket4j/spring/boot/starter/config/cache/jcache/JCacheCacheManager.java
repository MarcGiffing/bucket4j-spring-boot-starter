package com.giffing.bucket4j.spring.boot.starter.config.cache.jcache;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;

import javax.cache.Cache;

public class JCacheCacheManager<K, V> implements CacheManager<K, V> {

	private final Cache<K,V> cache;

	protected JCacheCacheManager(Cache<K, V> cache) {
		this.cache = cache;
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
