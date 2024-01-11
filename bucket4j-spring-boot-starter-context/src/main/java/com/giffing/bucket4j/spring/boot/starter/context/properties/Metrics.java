package com.giffing.bucket4j.spring.boot.starter.context.properties;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;

import lombok.Data;

@Data
public class Metrics implements Serializable {

	private boolean enabled = true;
	
	private List<MetricType> types = Arrays.asList(MetricType.values());
	
	private List<MetricTag> tags = new ArrayList<>();

}
