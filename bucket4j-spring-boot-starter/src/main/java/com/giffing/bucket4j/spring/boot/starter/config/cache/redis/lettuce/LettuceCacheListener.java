package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * This class is intended to be used as bean.
 *
 * It will listen to Lettuce events on the {cacheName}:update channel, parse them to CacheUpdateEvent<K, V>
 * and publish these to the Spring ApplicationEventPublisher
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
@Slf4j
public class LettuceCacheListener<K, V> extends RedisPubSubAdapter<String, String> {

	@Autowired
	private ApplicationEventPublisher eventPublisher;
	private final String cacheUpdateChannel;
	private final ObjectMapper objectMapper;
	private final JavaType deserializeType;

	/**
	 * @param redisClient The RedisClient to use for publishing/subscribing to events.
	 * @param cacheName The name of the cache. This is used as prefix for the event channels
	 * @param keyType The type of the key. This is required for parsing events and should match the K of this class.
	 * @param valueType The type of the value. This is required for parsing events and should match the V of this class.
	 */
	public LettuceCacheListener(RedisClient redisClient, String cacheName, Class<K> keyType, Class<V> valueType) {
		this.cacheUpdateChannel = cacheName.concat(":update");
		this.objectMapper = new ObjectMapper();
		this.deserializeType = objectMapper.getTypeFactory().constructParametricType(CacheUpdateEvent.class, keyType, valueType);

		StatefulRedisPubSubConnection<String, String> subConnection = redisClient.connectPubSub();
		subConnection.addListener(this);
		subConnection.async().subscribe(cacheUpdateChannel);
	}

	@Override
	public void message(String channel, String message) {
		if (channel.equals(cacheUpdateChannel)) {
			onCacheUpdateEvent(message);
		} else {
			log.debug("Unsupported cache event received on channel '{}'", channel);
		}
	}

	private void onCacheUpdateEvent(String message) {
		try {
			CacheUpdateEvent<K, V> updateEvent = objectMapper.readValue(message, deserializeType);
			this.eventPublisher.publishEvent(updateEvent);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
