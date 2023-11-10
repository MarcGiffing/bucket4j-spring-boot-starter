package com.giffing.bucket4j.spring.boot.starter.config.cache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
public class CacheUpdateEvent <K,V> {
	private K key;
	private V oldValue;
	private V newValue;
}
