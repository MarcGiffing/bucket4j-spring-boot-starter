package com.giffing.bucket4j.spring.boot.starter.context;

public enum RefillSpeed {

    /**
     * Greedily regenerates tokens.
     * <p>
     * The tokens are refilled as soon as possible.
     */
    GREEDY,

    /**
     * Regenerates tokens in an interval manner.
     * <p>
     * The tokens refilled on the specific defined interval.
     */
    INTERVAL,

}
