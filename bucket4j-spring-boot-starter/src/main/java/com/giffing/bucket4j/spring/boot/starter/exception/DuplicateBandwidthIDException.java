package com.giffing.bucket4j.spring.boot.starter.exception;

import lombok.Getter;

@Getter
public class DuplicateBandwidthIDException extends Bucket4jGeneralException {
    private static final long serialVersionUID = 1L;

    public DuplicateBandwidthIDException(String filterID, String bandwidthId) {
        super("Duplicate bandWidth id '" + bandwidthId + "' detected in filter '" + filterID + "'.");
    }
}
