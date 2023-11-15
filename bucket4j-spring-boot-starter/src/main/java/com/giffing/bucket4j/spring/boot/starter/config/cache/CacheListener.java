package com.giffing.bucket4j.spring.boot.starter.config.cache;

public interface CacheListener<K,V> {

	void addCacheUpdateListener(CacheUpdateListener<K,V> listener);

	void removeCacheUpdateListener(CacheUpdateListener<K,V> listener);

}
