package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.hazelcast.map.IMap;

public class HazelcastCacheManager<K, V> implements CacheManager<K, V> {

	private final IMap<K, V> map;

	public HazelcastCacheManager(IMap<K, V> map) {
		this.map = map;
	}

	@Override
	public V getValue(K key) {
		return this.map.get(key);
	}

	@Override
	public void setValue(K key, V value) {
		this.map.put(key, value);
	}
}
