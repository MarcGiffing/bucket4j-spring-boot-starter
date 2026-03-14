# Bucket4j PostgreSQL Cache Module

This module provides PostgreSQL database support for Bucket4j rate limiting with Spring Boot.

## Overview

The `cache-postgresql` module integrates Bucket4j with PostgreSQL, allowing you to store rate limit data in a PostgreSQL relational database. This is useful for distributed systems that already use PostgreSQL and want to leverage it for centralized rate limiting.

## Features

- **Synchronous Cache Access**: Provides synchronous rate limit token bucket operations
- **JDBC-based**: Uses Bucket4j's JDBC proxy manager for PostgreSQL connectivity
- **Spring Boot Auto-Configuration**: Automatic configuration when dependencies are present
- **Event Publishing**: Publishes cache update events to Spring's ApplicationEventPublisher
- **Configuration Caching**: Optional caching of Bucket4j configuration

## Dependencies

The module requires the following:

- `spring-boot-starter-data-jpa`: For JPA support (optional)
- `postgresql`: PostgreSQL JDBC driver
- `bucket4j_jdk17-jdbc`: Bucket4j JDBC support

## Configuration

To use the PostgreSQL cache module, add the following to your `application.properties` or `application.yml`:

```properties
bucket4j.enabled=true
bucket4j.cache-type=postgresql
```

### Database Setup

Before using the PostgreSQL cache, you need to create the necessary table for storing bucket tokens. Bucket4j uses a standard schema for JDBC storage.

Create the table with the following SQL:

```sql
CREATE TABLE IF NOT EXISTS bucket (
    id VARCHAR(20) PRIMARY KEY,
    state BYTEA,
    expires_at BIGINT,
    explicit_lock BIGINT);

CREATE INDEX IF NOT EXISTS idx_bucket4j_id ON bucket(id);
```

## Components

### PostgreSQLCacheResolver

Implements `SyncCacheResolver` and uses Bucket4j's `JdbcProxyManager` to manage rate limit buckets through JDBC connections.

**Key Features:**
- Synchronous access to rate limit tokens
- Automatic connection pooling through DataSource
- Direct integration with PostgreSQL via JDBC

### PostgreSQLCacheManager

Implements `CacheManager` for managing cache entries in the PostgreSQL database.

**Key Methods:**
- `getValue(K key)`: Retrieves cached values from the database
- `setValue(K key, V value)`: Stores or updates values in the database using PostgreSQL's `ON CONFLICT` clause

### PostgreSQLCacheListener

Listens to cache updates and publishes `CacheUpdateEvent` to the Spring ApplicationEventPublisher.

### PostgreSQLBucket4jConfiguration

Spring Boot auto-configuration class that:
- Checks if Bucket4j is enabled
- Validates DataSource availability
- Registers the `PostgreSQLCacheResolver` bean
- Optionally registers configuration cache manager
- Registers cache listener for event publishing

## Usage Example

```java
@RestController
@RequestMapping("/api")
public class MyController {
    
    @GetMapping("/data")
    @Bucket4j(bucketName = "main", capacityDescription = "10 requests per minute")
    public ResponseEntity<String> getData() {
        return ResponseEntity.ok("Hello World");
    }
}
```

## Configuration Properties

The following properties can be configured in `application.properties`:

```properties
# Enable Bucket4j
bucket4j.enabled=true

# Set cache type to PostgreSQL
bucket4j.cache-type=postgresql

# Optional: Cache configuration in PostgreSQL
bucket4j.filter-config-cache-enabled=true

# DataSource configuration (Spring Boot standard)
spring.datasource.url=jdbc:postgresql://localhost:5432/mydb
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver
```

## Performance Considerations

1. **Connection Pooling**: Ensure proper HikariCP (or other connection pool) configuration for optimal performance
2. **Table Indexing**: The `idx_bucket4j_tokens_id` index improves lookup performance
3. **Network Latency**: PostgreSQL-based rate limiting incurs network round-trip time, making it slower than in-memory solutions
4. **Distributed Systems**: Best suited for distributed systems where a centralized database is already in use

## Advantages

- **Centralized Rate Limiting**: All instances share the same rate limit state
- **Data Persistence**: Rate limit data survives application restarts
- **Simplicity**: Leverages existing PostgreSQL infrastructure
- **Scalability**: Works well in containerized and cloud environments

## Limitations

- **Performance**: Slower than in-memory cache solutions due to database I/O
- **Synchronous Only**: This module only provides synchronous cache access
- **Database Dependency**: Requires PostgreSQL to be available and operational

## Related Modules

- `cache-jcache`: JCache (JSR-107) implementation
- `cache-redis-lettuce`: Redis Lettuce driver support
- `cache-redis-jedis`: Redis Jedis driver support
- `cache-redis-redisson`: Redis Redisson driver support
- `cache-hazelcast`: Hazelcast distributed cache support
- `cache-infinispan`: Infinispan cache support

## License

Apache License 2.0

