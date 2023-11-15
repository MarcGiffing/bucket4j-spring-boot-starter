package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Slf4j
public class JedisCacheManager<K, V> extends CacheManager<K, V> {

	private final JedisPool pool;
	private final String cacheName;
	private final Class<V> valueType;
	private final ObjectMapper objectMapper;
	private final static String KEY_VALUE_EVENT_DELIMITER = "{split}";

	public JedisCacheManager(JedisPool pool, String cacheName, Class<K> keyType, Class<V> valueType) {
		super(new JedisCacheListener<>(cacheName, keyType, valueType, KEY_VALUE_EVENT_DELIMITER));
		this.pool = pool;
		this.cacheName = cacheName;
		this.valueType = valueType;
		this.objectMapper = new ObjectMapper();

		subscribe((JedisCacheListener<String, Bucket4JConfiguration>) super.cacheListener);
	}

	public void subscribe(JedisCacheListener<String, Bucket4JConfiguration> listener) {
		new Thread(() -> {
			try (Jedis jedis = pool.getResource()) {
				jedis.subscribe(listener, cacheName);
			} catch (Exception e) {
				log.warn("Failed to instantiate the Jedis subscriber. {}",e.getMessage());
			}
		}, "JedisSubscriberThread").start();
	}

	@Override
	public V getValue(K key) {
		try (Jedis jedis = pool.getResource()) {
			String serializedValue = jedis.hget(cacheName, objectMapper.writeValueAsString(key));
			return serializedValue != null ? objectMapper.readValue(serializedValue, this.valueType) : null;
		} catch (JsonProcessingException e) {
			return null;
		}
	}

	@Override
	public void setValue(K key, V value) {
		try (Jedis jedis = pool.getResource()) {
			String serializedKey = objectMapper.writeValueAsString(key);
			String serializedValue = objectMapper.writeValueAsString(value);
			jedis.hset(this.cacheName, serializedKey, serializedValue);

			jedis.publish(cacheName, serializedKey + KEY_VALUE_EVENT_DELIMITER + serializedValue);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
