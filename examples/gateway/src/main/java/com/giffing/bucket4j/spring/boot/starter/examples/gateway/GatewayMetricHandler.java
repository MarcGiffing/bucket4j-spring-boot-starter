package com.giffing.bucket4j.spring.boot.starter.examples.gateway;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;


@Component
@Slf4j
public class GatewayMetricHandler implements MetricHandler {

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {
		log.info(String.format("name: %s;type: %s; tokens: %s", type, name, tokens));
		log.info("\t" + tags.stream().map(mt -> mt.getKey() + ":" + mt.getValue()).collect(Collectors.joining(",")));
		log.info("################");
		
	}

}
