package com.giffing.bucket4j.spring.boot.starter.config.refresh;

import org.springframework.context.ApplicationEvent;

/**
 * This is an {@link ApplicationEvent} which triggers the reload Bucket4j configuration.
 *
 */
public class Bucket4jRefreshEvent extends ApplicationEvent {

	public Bucket4jRefreshEvent(Object source) {
		super(source);
	}

}
