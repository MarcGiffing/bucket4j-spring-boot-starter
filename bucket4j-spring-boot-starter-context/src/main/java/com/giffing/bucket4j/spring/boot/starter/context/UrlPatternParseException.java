package com.giffing.bucket4j.spring.boot.starter.context;

public class UrlPatternParseException extends Exception {

    public UrlPatternParseException() {
    }

    public UrlPatternParseException(String message) {
        super(message);
    }

    public UrlPatternParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public UrlPatternParseException(Throwable cause) {
        super(cause);
    }

    public UrlPatternParseException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
