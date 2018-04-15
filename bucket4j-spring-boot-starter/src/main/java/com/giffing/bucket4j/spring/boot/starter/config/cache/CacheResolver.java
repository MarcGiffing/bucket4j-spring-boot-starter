package com.giffing.bucket4j.spring.boot.starter.config.cache;

import java.io.Serializable;

import io.github.bucket4j.grid.ProxyManager;

public interface CacheResolver {

	ProxyManager<String> resolve(String cacheName);
	
}
