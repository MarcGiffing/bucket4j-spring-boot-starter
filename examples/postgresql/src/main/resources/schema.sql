CREATE TABLE IF NOT EXISTS bucket (
        id VARCHAR(20) PRIMARY KEY,
        state BYTEA,
         expires_at BIGINT,
         explicit_lock BIGINT);
CREATE INDEX IF NOT EXISTS idx_bucket4j_id ON bucket(id);