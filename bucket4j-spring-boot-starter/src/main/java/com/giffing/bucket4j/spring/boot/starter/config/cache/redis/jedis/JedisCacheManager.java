package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class JedisCacheManager<K, V> implements CacheManager<K, V> {

	private final JedisPool pool;
	private final String cacheName;
	private final Class<V> valueType;
	private final ObjectMapper objectMapper;
	private final String updateChannel;

	/**
	 * @param pool The JedisPool to use for reading/writing data to the cache
	 * @param cacheName The name of the cache.
	 * @param valueType The type of the data. This is required for parsing and should always match the V of this class.
	 */
	public JedisCacheManager(JedisPool pool, String cacheName, Class<V> valueType) {
		this.pool = pool;
		this.cacheName = cacheName;
		this.valueType = valueType;

		this.objectMapper = new ObjectMapper();
		this.updateChannel = cacheName.concat(":update");
	}


	@Override
	public V getValue(K key) {
		try (Jedis jedis = pool.getResource()) {
			String serializedValue = jedis.hget(cacheName, objectMapper.writeValueAsString(key));
			return serializedValue != null ? objectMapper.readValue(serializedValue, this.valueType) : null;
		} catch (JsonProcessingException e) {
			log.warn("Exception occurred while retrieving key '{}' from cache '{}'. Message: {}", key, cacheName, e.getMessage());
			return null;
		}
	}

	@Override
	public void setValue(K key, V value) {
		try (Jedis jedis = pool.getResource()) {
			V oldValue = getValue(key);

			String serializedKey = objectMapper.writeValueAsString(key);
			String serializedValue = objectMapper.writeValueAsString(value);
			jedis.hset(this.cacheName, serializedKey, serializedValue);

			//publish an update event if the key already existed
			if(oldValue != null){
				CacheUpdateEvent<K,V> updateEvent = new CacheUpdateEvent<>(key, oldValue, value);
				jedis.publish(this.updateChannel, objectMapper.writeValueAsString(updateEvent));
			}
		} catch (JsonProcessingException e) {
			log.warn("Exception occurred while setting key '{}' in cache '{}'. Message: {}", key, cacheName, e.getMessage());
			throw new RuntimeException(e);
		}
	}
}
