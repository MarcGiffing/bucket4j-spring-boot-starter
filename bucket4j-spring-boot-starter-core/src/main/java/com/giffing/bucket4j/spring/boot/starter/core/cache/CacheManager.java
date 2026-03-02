package com.giffing.bucket4j.spring.boot.starter.core.cache;

public interface CacheManager<K, V> {
	 V getValue(K key);
	 void setValue(K key, V value);
}
