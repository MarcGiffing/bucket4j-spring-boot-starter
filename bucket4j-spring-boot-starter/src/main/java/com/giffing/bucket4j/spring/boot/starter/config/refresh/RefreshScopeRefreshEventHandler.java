package com.giffing.bucket4j.spring.boot.starter.config.refresh;

import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;

/**
 * This handler is an intermediate handler the {@link RefreshScopeRefreshedEvent}. If the 
 * {@link RefreshScopeRefreshedEvent} is received an internal {@link Bucket4jRefreshEvent} is thrown.
 */
public class RefreshScopeRefreshEventHandler {

	private ApplicationEventPublisher applicationEventPublisher;
	
	public RefreshScopeRefreshEventHandler(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}
	
	@EventListener
	public void handleRefreshEvent(RefreshScopeRefreshedEvent refreshEvent) {
		applicationEventPublisher.publishEvent(new Bucket4jRefreshEvent(refreshEvent));
	}

	
	
}