package com.giffing.bucket4j.spring.boot.starter.config.cache;

public abstract class CacheManager<K, V> {
	protected final CacheListener<K, V> cacheListener;

	protected CacheManager(CacheListener<K, V> cacheListener) {
		this.cacheListener = cacheListener;
	}

	abstract public V getValue(K key);

	abstract public void setValue(K key, V value);

	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.cacheListener.addCacheUpdateListener(listener);
	}

}
