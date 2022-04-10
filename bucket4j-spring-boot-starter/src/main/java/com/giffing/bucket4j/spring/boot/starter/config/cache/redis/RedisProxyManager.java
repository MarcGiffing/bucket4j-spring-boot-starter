package com.giffing.bucket4j.spring.boot.starter.config.cache.redis;

import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.distributed.proxy.ClientSideConfig;
import io.github.bucket4j.distributed.remote.AbstractBinaryTransaction;
import io.github.bucket4j.distributed.remote.CommandResult;
import io.github.bucket4j.distributed.remote.Request;
import io.github.bucket4j.distributed.serialization.InternalSerializationHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.CompletableFuture;

/**
 * The extension of Bucket4j library addressed to support Redis.
 */
class RedisProxyManager extends AbstractProxyManager<String> {

    private final RedisTemplate<String, byte[]> redisTemplate;
    private final String cacheName;

    protected RedisProxyManager(RedisTemplate<String, byte[]> redisTemplate, String cacheName) {
        super(ClientSideConfig.getDefault());
        this.redisTemplate = redisTemplate;
        this.cacheName = cacheName;
    }

    @Override
    public <T> CommandResult<T> execute(String key, Request<T> request) {
        byte[] resultBytes = new BucketProcessor<T>(redisTemplate).process(buildRedisKey(key), request);
        return InternalSerializationHelper.deserializeResult(resultBytes, request.getBackwardCompatibilityVersion());
    }

    @Override
    public void removeProxy(String key) {
        redisTemplate.delete(buildRedisKey(key));
    }

    @Override
    public boolean isAsyncModeSupported() {
        return false;
    }

    @Override
    public <T> CompletableFuture<CommandResult<T>> executeAsync(String key, Request<T> request) {
        // not supported yet
        throw new UnsupportedOperationException();
    }

    @Override
    protected CompletableFuture<Void> removeAsync(String key) {
        // not supported yet
        throw new UnsupportedOperationException();
    }

    private String buildRedisKey(String key) {
        return cacheName + "." + key;
    }

    private static class BucketProcessor<T> {

        private final RedisTemplate<String, byte[]> redisTemplate;

        public BucketProcessor(RedisTemplate<String, byte[]> redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        public byte[] process(String key, Request<T> request) {
            return new RedisTransaction(redisTemplate, key, InternalSerializationHelper.serializeRequest(request)).execute();
        }
    }

    private static class RedisTransaction extends AbstractBinaryTransaction {

        private final RedisTemplate<String, byte[]> redisTemplate;
        private final String key;

        private RedisTransaction(RedisTemplate<String, byte[]> redisTemplate, String key, byte[] requestBytes) {
            super(requestBytes);
            this.redisTemplate = redisTemplate;
            this.key = key;
        }

        @Override
        public boolean exists() {
            return redisTemplate.hasKey(key);
        }

        @Override
        protected byte[] getRawState() {
            return redisTemplate.opsForValue().get(key);
        }

        @Override
        protected void setRawState(byte[] stateBytes) {
            redisTemplate.opsForValue().set(key, stateBytes);
        }
    }
}
