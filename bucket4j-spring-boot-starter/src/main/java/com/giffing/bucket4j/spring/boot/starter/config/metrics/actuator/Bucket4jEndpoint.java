package com.giffing.bucket4j.spring.boot.starter.config.metrics.actuator;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Gateway;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Servlet;
import com.giffing.bucket4j.spring.boot.starter.context.qualifier.Webflux;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnClass(Endpoint.class)
public class Bucket4jEndpoint {

	@Configuration
	@Endpoint(id = "bucket4j")
	public static class Bucket4jEndpointConfig {

		private final Bucket4jConfigurationHolder servletConfigs;
		
		private final Bucket4jConfigurationHolder webfluxConfigs;
		
		private final Bucket4jConfigurationHolder gatewayConfigs;

		public Bucket4jEndpointConfig(
				@Autowired(required = false)
				@Servlet
				Bucket4jConfigurationHolder servletConfigs,
				@Autowired(required = false)
				@Webflux
				Bucket4jConfigurationHolder webfluxConfigs,
				@Autowired(required = false)
				@Gateway
				Bucket4jConfigurationHolder gatewayConfigs) {
			this.servletConfigs = servletConfigs;
			this.webfluxConfigs = webfluxConfigs;
			this.gatewayConfigs = gatewayConfigs;
		}


		@ReadOperation
		public Map<String, Object> bucket4jConfig() {
			Map<String, Object> result = new HashMap<>();
			if(servletConfigs != null) {
				result.put("servlet", servletConfigs.getFilterConfiguration());
			}
			if(webfluxConfigs != null) {
				result.put("webflux", webfluxConfigs.getFilterConfiguration());
			}
			
			if(gatewayConfigs != null) {
				result.put("gateway", gatewayConfigs.getFilterConfiguration());
			}
			
			return result;
		}
		
	}

}
