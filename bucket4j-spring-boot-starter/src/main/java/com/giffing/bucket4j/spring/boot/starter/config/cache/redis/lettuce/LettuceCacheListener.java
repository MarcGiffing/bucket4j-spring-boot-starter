package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.lettuce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import io.lettuce.core.RedisClient;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;

@Slf4j
public class LettuceCacheListener<K, V> extends RedisPubSubAdapter<String, String> {

	private final ApplicationEventPublisher eventPublisher;
	private final String cacheUpdateChannel;
	private final ObjectMapper objectMapper;
	private final JavaType deserializeType;

	public LettuceCacheListener(ApplicationEventPublisher eventPublisher, RedisClient redisClient, String cacheName, Class<K> keyType, Class<V> valueType) {
		this.eventPublisher = eventPublisher;
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
			log.debug("Unsupported cache event received of type ");
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
