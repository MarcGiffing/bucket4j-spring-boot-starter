package com.giffing.bucket4j.spring.boot.starter.examples.ehcache;

import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties
public class UriConfiguration {

	private String httpbin = "http://httpbin.org:80";

	public String getHttpbin() {
		return httpbin;
	}

	public void setHttpbin(String httpbin) {
		this.httpbin = httpbin;
	}
}
