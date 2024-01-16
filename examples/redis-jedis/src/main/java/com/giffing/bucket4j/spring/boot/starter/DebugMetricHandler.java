package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class DebugMetricHandler implements MetricHandler {

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {
	log.info(String.format("type: %s; name: %s; tags: %s; tokens: %s",
				type,
				name,
				tags
					.stream()
					.map(mtr -> mtr.getKey() + ":" + mtr.getValue())
					.collect(Collectors.joining(",")),
				tokens));
		
	}

}
