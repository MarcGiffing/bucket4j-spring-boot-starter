package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPubSub;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class JedisCacheListener<K,V> extends JedisPubSub implements CacheListener<K,V> {

	private final List<CacheUpdateListener<K,V>> updateListeners = new ArrayList<>();
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String cacheName;
	private final Class<K> keyType;
	private final Class<V> valueType;
	private final String keyValueDelimiterPattern;
	public JedisCacheListener(String cacheName, Class<K> keyType, Class<V> valueType, String keyValueDelimiter){
		this.cacheName = cacheName;
		this.keyType = keyType;
		this.valueType = valueType;
		keyValueDelimiterPattern = Pattern.quote(keyValueDelimiter);
	}

	@Override
	public void onMessage(String channel, String message) {
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

	@Override
	public void addCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		updateListeners.add(listener);
	}

	@Override
	public void removeCacheUpdateListener(CacheUpdateListener<K, V> listener) {
		updateListeners.remove(listener);
	}
}
