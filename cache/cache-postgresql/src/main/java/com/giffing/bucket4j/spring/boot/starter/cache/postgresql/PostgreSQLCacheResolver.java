package com.giffing.bucket4j.spring.boot.starter.cache.postgresql;

import com.giffing.bucket4j.spring.boot.starter.core.cache.AbstractCacheResolverTemplate;
import com.giffing.bucket4j.spring.boot.starter.core.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import io.github.bucket4j.distributed.jdbc.PrimaryKeyMapper;
import io.github.bucket4j.distributed.proxy.AbstractProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.postgresql.Bucket4jPostgreSQL;

import javax.sql.DataSource;

/**
 * This class is the PostgreSQL (JDBC) implementation of the {@link CacheResolver}.
 * It uses Bucket4Js {@link io.github.bucket4j.postgresql.PostgreSQLadvisoryLockBasedProxyManager} to implement the {@link ProxyManager}.
 */
public class PostgreSQLCacheResolver extends AbstractCacheResolverTemplate<String> implements SyncCacheResolver {

    private final DataSource dataSource;

    public PostgreSQLCacheResolver(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public String castStringToCacheKey(String key) {
        return key;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public AbstractProxyManager<String> getProxyManager(String cacheName) {
        return Bucket4jPostgreSQL.selectForUpdateBasedBuilder(dataSource)
                .primaryKeyMapper(PrimaryKeyMapper.STRING)
                .build();
    }
}

