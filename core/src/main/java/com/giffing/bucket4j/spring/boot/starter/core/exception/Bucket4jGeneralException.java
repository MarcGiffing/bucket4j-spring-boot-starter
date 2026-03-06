package com.giffing.bucket4j.spring.boot.starter.core.exception;


import java.io.Serial;

/**
 * All exceptions should be extend from the this base exception.
 * The Bucket4JAutoConfigFailureAnalyzer uses this class as a base class to analyze
 * the exception on startup.
 *
 */
public abstract class Bucket4jGeneralException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    protected Bucket4jGeneralException() {
        super();
    }

    protected Bucket4jGeneralException(String message) {
        super(message);
    }

}
