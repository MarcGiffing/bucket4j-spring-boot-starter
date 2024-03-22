package com.giffing.bucket4j.spring.boot.starter.context;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * This data class holds the information of a rate limit check.
 */
@Data
@Builder
public class RateLimitResult {

    /**
     * If the request was only for estimation without consuming any tokens.
     */
    @NonNull
    private final boolean estimation;

    /**
     * The tokens that are consumed.
     * <p>
     * If {@link #estimation} is true no tokens are consumed.
     */
    @NonNull
    private final boolean consumed;

    /**
     * The number of tokens that remains until rate limit is executed.
     */
    @NonNull
    private final long remainingTokens;

    /**
     * The time in nanoseconds until the next tokens will be refilled.
     */
    @NonNull
    private final long nanosToWaitForRefill;

    /**
     * The time in nanoseconds until the tokens are refilled to its maximum.
     */
    @NonNull
    private final long nanosToWaitForReset;
}
