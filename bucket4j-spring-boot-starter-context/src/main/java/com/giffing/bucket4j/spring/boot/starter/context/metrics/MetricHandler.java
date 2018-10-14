package com.giffing.bucket4j.spring.boot.starter.context.metrics;

import java.util.List;

public interface MetricHandler {

    void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags);

}
