package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LettuceCacheManager<K, V> implements CacheManager<K, V> {
	private final RedisCommands<String, String> syncCommands;
	private final String cacheName;
	private final Class<V> valueType;
	private final ObjectMapper objectMapper;
	private final String cacheUpdateChannel;

	/**
	 * @param redisClient The RedisClient to use for reading/writing data to the cache
	 * @param cacheName The name of the cache.
	 * @param valueType The type of the data. This is required for parsing and should always match the V of this class.
	 */
	protected LettuceCacheManager(RedisClient redisClient, String cacheName, Class<V> valueType) {
		this.syncCommands = redisClient.connect().sync();
		this.cacheName = cacheName;
		this.valueType = valueType;
		this.objectMapper = new ObjectMapper();
		this.cacheUpdateChannel = cacheName.concat(":update");
	}

	@Override
	public V getValue(K key) {
		try {
			String serializedValue = syncCommands.hget(cacheName, objectMapper.writeValueAsString(key));
			return serializedValue != null ? objectMapper.readValue(serializedValue, this.valueType) : null;
		} catch (JsonProcessingException e) {
			log.warn("Exception occurred while retrieving key '{}' from cache '{}'. Message: {}", key, cacheName, e.getMessage());
			return null;
		}
	}

	@Override
	public void setValue(K key, V value) {
		try {
			V oldValue = getValue(key);

			String serializedKey = objectMapper.writeValueAsString(key);
			String serializedValue = objectMapper.writeValueAsString(value);
			syncCommands.hset(this.cacheName, serializedKey, serializedValue);

			//if the key did not exist yet, publish an update event
			if (oldValue != null) {
				CacheUpdateEvent<K, V> updateEvent = new CacheUpdateEvent<>(key, oldValue, value);
				syncCommands.publish(cacheUpdateChannel, objectMapper.writeValueAsString(updateEvent));
			}
		} catch (JsonProcessingException e) {
			log.warn("Exception occurred while setting key '{}' in cache '{}'. Message: {}", key, cacheName, e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
