package com.giffing.bucket4j.spring.boot.starter.cache.postgresql;

import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnCache;
import com.giffing.bucket4j.spring.boot.starter.autoconfigure.conditional.ConditionalOnSynchronousPropertyCondition;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.core.cache.SyncCacheResolver;
import io.github.bucket4j.postgresql.Bucket4jPostgreSQL;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

import javax.sql.DataSource;

@AutoConfiguration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBucket4jEnabled
@ConditionalOnSynchronousPropertyCondition
@ConditionalOnClass(Bucket4jPostgreSQL.class)
@ConditionalOnCache("postgresql")
@EnableConfigurationProperties({Bucket4JBootProperties.class})
public class PostgreSQLBucket4jConfiguration {

    private final DataSource dataSource;

    public PostgreSQLBucket4jConfiguration(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean
    @ConditionalOnMissingBean(SyncCacheResolver.class)
    public SyncCacheResolver bucket4jCacheResolver() {
        return new PostgreSQLCacheResolver(dataSource);
    }

}



