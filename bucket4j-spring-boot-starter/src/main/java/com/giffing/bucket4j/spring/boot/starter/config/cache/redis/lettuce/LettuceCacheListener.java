package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LettuceCacheListener<K, V> extends RedisPubSubAdapter<String, String> implements CacheListener<K, V> {
	private final String cacheName;
	private final ObjectMapper objectMapper;
	private final List<CacheUpdateListener<K, V>> updateListeners = new ArrayList<>();

	private final JavaType deserializeType;

	public LettuceCacheListener(String cacheName, Class<K> keyType, Class<V> valueType) {
		this.cacheName = cacheName;
		this.objectMapper = new ObjectMapper();
		this.deserializeType = objectMapper.getTypeFactory().constructParametricType(CacheUpdateEvent.class, keyType, valueType);
	}

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.updateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		this.updateListeners.remove(listener);
	}

	@Override
	public void message(String channel, String message) {
		if (channel.equals(cacheName.concat(":update"))) {
			onCacheUpdateEvent(message);
		} else {
			log.debug("Unsupported cache event received of type ");
		}
	}

	private void onCacheUpdateEvent(String message) {
		try {
			CacheUpdateEvent<K, V> event = objectMapper.readValue(message, deserializeType);
			this.updateListeners.forEach(x -> x.onCacheUpdateEvent(event));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
