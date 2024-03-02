package com.giffing.bucket4j.spring.boot.starter.context;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class RateLimitResult {

    @NonNull
    private final boolean estimation;

    @NonNull
    private final boolean consumed;

    @NonNull
    private final long remainingTokens;

    @NonNull
    private final long nanosToWaitForRefill;

    @NonNull
    private final long nanosToWaitForReset;
}
