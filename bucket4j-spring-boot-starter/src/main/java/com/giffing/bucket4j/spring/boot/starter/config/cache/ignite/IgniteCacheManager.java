package com.giffing.bucket4j.spring.boot.starter.config.cache.ignite;


import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import org.apache.ignite.IgniteCache;

public class IgniteCacheManager<K,V> implements CacheManager<K, V> {

	private final IgniteCache<K,V> cache;

	public IgniteCacheManager(IgniteCache<K,V> cache){
		this.cache = cache;
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
