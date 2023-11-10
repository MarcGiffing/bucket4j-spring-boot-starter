package com.giffing.bucket4j.spring.boot.starter.config.cache.hazelcast;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.hazelcast.map.IMap;

public class HazelcastCacheManager<K, V> extends CacheManager<K, V> {

	private final IMap<K, V> map;

	public HazelcastCacheManager(IMap<K, V> map) {
		super(new HazelcastCacheListener<>());
		this.map = map;
		this.map.addEntryListener((HazelcastCacheListener<K, V>) super.cacheListener, true);
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
