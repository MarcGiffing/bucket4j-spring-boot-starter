package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import org.redisson.api.RMap;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.io.Serializable;


public class RedissonCacheManager<K extends Serializable, V> implements CacheManager<K, V> {
	private final String cacheName;
	private final RedissonClient redisson;
	private final RTopic pubSubTopic;

	protected RedissonCacheManager(RedissonClient redisson, String cacheName) {
		this.cacheName = cacheName;
		this.redisson = redisson;

		this.pubSubTopic = redisson.getTopic(cacheName);
	}

	@Override
	public V getValue(K key) {
		RMap<K, V> map = this.redisson.getMap(this.cacheName);
		return map.get(key);
	}

	@Override
	public void setValue(K key, V value) {
		RMap<K, V> map = this.redisson.getMap(this.cacheName);
		V oldValue = map.put(key, value);

		//publish an update event if the key already existed
		if (oldValue != null) {
			CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(key, oldValue, value);
			this.pubSubTopic.publish(updateEvent);
		}
	}
}
