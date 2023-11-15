package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class LettuceCacheListener<K, V> extends RedisPubSubAdapter<String, String> implements CacheListener<K, V> {
	private final String cacheName;
	private final Class<K> keyType;
	private final Class<V> valueType;
	private final String keyValueDelimiterPattern;
	private final ObjectMapper objectMapper;
	private final List<CacheUpdateListener<K,V>> updateListeners = new ArrayList<>();
	public LettuceCacheListener(String cacheName, Class<K> keyType, Class<V> valueType, String keyValueDelimiter) {
		this.cacheName = cacheName;
		this.keyType = keyType;
		this.valueType = valueType;
		this.keyValueDelimiterPattern = Pattern.quote(keyValueDelimiter);
		this.objectMapper = new ObjectMapper();
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
		if(channel.equalsIgnoreCase(cacheName)){
			String[] parts = message.split(keyValueDelimiterPattern, 2);
			if (parts.length == 2) {
				String serializedKey = parts[0];
				String serializedValue = parts[1];

				try {
					K key = objectMapper.readValue(serializedKey, keyType);
					V value = objectMapper.readValue(serializedValue, valueType);

					this.updateListeners.forEach(x -> x.onCacheUpdateEvent(new CacheUpdateEvent<>(key, null, value)));
				} catch (JsonProcessingException e) {
					log.debug("Failed to process Jedis Message. {}", e.getMessage());
				}
			}
		}
	}
}
