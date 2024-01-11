package com.giffing.bucket4j.spring.boot.starter.config.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheUpdateEvent <K,V> {
	private K key;
	private V oldValue;
	private V newValue;
}
