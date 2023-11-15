package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

public class LettuceCacheManager<K,V> extends CacheManager<K, V> {

	private final static String KEY_VALUE_EVENT_DELIMITER = "{split}";
	private final String cacheName;
	private final ObjectMapper objectMapper;
	private final RedisCommands<String, String> syncCommands;
	private final Class<V> valueType;

	protected LettuceCacheManager(RedisClient redisClient, String cacheName, Class<K> keyType, Class<V> valueType) {
		super(new LettuceCacheListener<>(cacheName, keyType, valueType, KEY_VALUE_EVENT_DELIMITER));
		this.cacheName = cacheName;
		this.objectMapper = new ObjectMapper();
		this.valueType = valueType;
		this.syncCommands = redisClient.connect().sync();

		StatefulRedisPubSubConnection<String, String> subConnection = redisClient.connectPubSub();
		subConnection.addListener((LettuceCacheListener<K,V>) super.cacheListener);
		subConnection.async().subscribe(cacheName);
	}

	@Override
	public V getValue(K key) {
		try{
			String serializedValue = syncCommands.hget(cacheName, objectMapper.writeValueAsString(key));
			return serializedValue != null ? objectMapper.readValue(serializedValue, this.valueType) : null;
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public void setValue(K key, V value) {
		try{
			String serializedKey = objectMapper.writeValueAsString(key);
			String serializedValue = objectMapper.writeValueAsString(value);
			syncCommands.hset(this.cacheName, serializedKey, serializedValue);

			syncCommands.publish(cacheName, serializedKey + KEY_VALUE_EVENT_DELIMITER + serializedValue);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
