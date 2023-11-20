package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class JedisCacheListener<K, V> extends JedisPubSub implements CacheListener<K, V> {

	private final List<CacheUpdateListener<K, V>> updateListeners = new ArrayList<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String updateChannel;
	private final JavaType deserializeType;

	public JedisCacheListener(String cacheName, Class<K> keyType, Class<V> valueType) {
		this.updateChannel = cacheName.concat(":update");
		this.deserializeType = objectMapper.getTypeFactory().constructParametricType(CacheUpdateEvent.class, keyType, valueType);
	}

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		updateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		updateListeners.remove(listener);
	}

	@Override
	public void onMessage(String channel, String message) {
		if (channel.equals(updateChannel)) {
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