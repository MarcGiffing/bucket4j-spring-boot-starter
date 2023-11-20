package com.giffing.bucket4j.spring.boot.starter.examples.ehcache;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;


@Component
public class GatewayMetricHandler implements MetricHandler {

	@Override
	public void handle(MetricType type, String name, long tokens, List<MetricTagResult> tags) {
		System.out.println(String.format("name: %s;type: %s; tokens: %s", type, name, tokens));
		System.out.println("\t" + tags.stream().map(mt -> mt.getKey() + ":" + mt.getValue()).collect(Collectors.joining(",")));
		System.out.println("################");
		
	}

}
