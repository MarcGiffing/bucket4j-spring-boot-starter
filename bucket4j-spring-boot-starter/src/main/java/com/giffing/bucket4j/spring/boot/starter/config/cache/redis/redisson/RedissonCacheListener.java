package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.redisson;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import org.redisson.api.RTopic;

import java.util.ArrayList;
import java.util.List;

public class RedissonCacheListener<K,V> implements CacheListener<K,V> {

	private final List<CacheUpdateListener<K,V>> updateListeners = new ArrayList<>();

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.updateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.updateListeners.remove(listener);
	}

	public void onCacheUpdateEvent(CacheUpdateEvent<K, V> event) {
		this.updateListeners.forEach(x -> x.onCacheUpdateEvent(event));
	}

	public void init(RTopic pubSubTopic) {
		//initialize the update listener
		pubSubTopic.addListener(CacheUpdateEvent.class,
				(CharSequence channel, CacheUpdateEvent<K,V> event) -> this.updateListeners.forEach(x -> onCacheUpdateEvent(event)));
	}
}
