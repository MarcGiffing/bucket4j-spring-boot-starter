package com.giffing.bucket4j.spring.boot.starter.config.refresh;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to reload the Bucket4j Configuration when the {@link RefreshScopeRefreshedEvent} is fired.
 */
@Configuration
@ConditionalOnClass(RefreshScopeRefreshedEvent.class)
public class ApplicationRefreshConfiguration {
	
	@Bean
	public RefreshScopeRefreshEventHandler refreshHandler(ApplicationEventPublisher applicationEventPublisher) {
		return new RefreshScopeRefreshEventHandler(applicationEventPublisher);
	}
	
}
