package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Data
public class MetricTag implements Serializable {

	private String key;

	@NotBlank
	private String expression;
	
	private List<MetricType> types = Arrays.asList(MetricType.values());
	
}
