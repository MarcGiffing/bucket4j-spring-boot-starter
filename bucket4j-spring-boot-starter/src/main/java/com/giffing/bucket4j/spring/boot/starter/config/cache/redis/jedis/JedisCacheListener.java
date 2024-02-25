package com.giffing.bucket4j.spring.boot.starter.config.cache.redis.jedis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateEvent;
import io.micrometer.core.instrument.util.NamedThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is intended to be used as bean.
 *
 * It will listen to Jedis events on the {cacheName}:update channel, parse them to  {@code CacheUpdateEvent<K, V>}
 * and publish these to the Spring ApplicationEventPublisher
 *
 * @param <K> Type of the cache key
 * @param <V> Type of the cache value
 */
@Slf4j
public class JedisCacheListener<K, V> extends JedisPubSub {

	private final JedisPool jedisPool;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final String updateChannel;
	private final JavaType deserializeType;
	private ApplicationEventPublisher eventPublisher;

	/**
	 * @param jedisPool The pool to use for listening/publishing events
	 * @param cacheName The name of the cache. This is used as prefix for the event channels
	 * @param keyType The type of the key. This is required for parsing events and should match the K of this class.
	 * @param valueType The type of the value. This is required for parsing events and should match the V of this class.
	 */
	public JedisCacheListener(JedisPool jedisPool, String cacheName, Class<K> keyType, Class<V> valueType, ApplicationEventPublisher eventPublisher) {
		this.jedisPool = jedisPool;
		this.updateChannel = cacheName.concat(":update");
		this.deserializeType = objectMapper.getTypeFactory().constructParametricType(CacheUpdateEvent.class, keyType, valueType);
		this.eventPublisher = eventPublisher;
		subscribe();
	}

	public void subscribe() {
		Thread thread = new Thread(() -> {
			AtomicInteger reconnectBackoffTimeMillis = new AtomicInteger(1000);
			// Using a NamedThreadFactory for creating a Daemon thread, so it will never block the jvm from closing.
			ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("reset-reconnect-backoff-thread"));
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
						// exponentially increase the backoff with a max of 30 seconds
						reconnectBackoffTimeMillis.set(Math.min((reconnectBackoffTimeMillis.get() * 2), 30000));
					} catch (InterruptedException ignored) {
						// ignored, already interrupted so the while loop will stop
					}
				}
			}
		}, "JedisSubscriberThread");
		thread.setDaemon(true);
		thread.start();
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