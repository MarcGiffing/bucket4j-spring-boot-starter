package com.giffing.bucket4j.spring.boot.starter.examples.postgresql;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class DebugMetricHandler implements MetricHandler {

	private final BucketRepository bucketRepository;

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {
		bucketRepository.findAll().forEach(b -> log.info(b.toString()));
		log.info("type: {}; name: {}; tags: {}",
                type,
                name,
                tags
                        .stream()
                        .map(mtr -> mtr.getKey() + ":" + mtr.getValue())
                        .collect(Collectors.joining(",")));
		
	}

}
