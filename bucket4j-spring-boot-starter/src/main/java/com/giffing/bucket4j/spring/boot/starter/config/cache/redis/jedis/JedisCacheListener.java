package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * This class is intended to be used as bean.
 *
 * It will listen to Jedis events on the {cacheName}:update channel, parse them to CacheUpdateEvent<K, V>
 * and publish these to the Spring ApplicationEventPublisher
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
@Slf4j
public class JedisCacheListener<K, V> extends JedisPubSub {

	@Autowired
	private ApplicationEventPublisher eventPublisher;
	private final JedisPool jedisPool;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String updateChannel;
	private final JavaType deserializeType;

	/**
	 * @param jedisPool The pool to use for listening/publishing events
	 * @param cacheName The name of the cache. This is used as prefix for the event channels
	 * @param keyType The type of the key. This is required for parsing events and should match the K of this class.
	 * @param valueType The type of the value. This is required for parsing events and should match the V of this class.
	 */
	public JedisCacheListener(JedisPool jedisPool, String cacheName, Class<K> keyType, Class<V> valueType) {
		this.jedisPool = jedisPool;
		this.updateChannel = cacheName.concat(":update");
		this.deserializeType = objectMapper.getTypeFactory().constructParametricType(CacheUpdateEvent.class, keyType, valueType);

		subscribe();
	}

	public void subscribe() {
		new Thread(() -> {
			AtomicInteger reconnectBackoffTimeMillis = new AtomicInteger(1000);
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
			ScheduledFuture<?> resetTask = null;

			while(!Thread.currentThread().isInterrupted() && !this.jedisPool.isClosed()){
				try (Jedis jedis = this.jedisPool.getResource()) {
					// Schedule a reset of the backoff after 10 seconds.
					// This is done in a different thread since subscribe is a blocking call.
					resetTask = executorService.schedule(()-> reconnectBackoffTimeMillis.set(1000), 10000, TimeUnit.MILLISECONDS);

					jedis.subscribe(this, updateChannel);
				} catch (Exception e) {
					log.error("Failed to connect the Jedis subscriber, attempting to reconnect in {} seconds. " +
							"Exception was: {}", (reconnectBackoffTimeMillis.get() /1000), e.getMessage());

					// Cancel the reset of the backoff
					if(resetTask != null) {
						resetTask.cancel(true);
						resetTask = null;
					}

					// Wait before trying to reconnect and increase the backoff duration
					try {
						Thread.sleep(reconnectBackoffTimeMillis.get());
						//exponential increase backoff with a max of 1 minute
						reconnectBackoffTimeMillis.set(Math.min((reconnectBackoffTimeMillis.get() * 2), 60000));
					} catch (InterruptedException ignored) {
						Thread.currentThread().interrupt();
					}
				}
			}
		}, "JedisSubscriberThread").start();
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
			this.eventPublisher.publishEvent(event);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}