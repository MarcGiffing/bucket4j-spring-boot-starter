package com.giffing.bucket4j.spring.boot.starter.springboot1.actuator;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;

@Configuration
@ConditionalOnClass(AbstractEndpoint.class)
public class Bucket4jEndpoint extends AbstractEndpoint<Map<String, Object>> implements EnvironmentAware  {

	@Autowired(required = false)
	@Qualifier("SERVLET")
	private Bucket4jConfigurationHolder servletConfigs;
	
	@Autowired(required = false)
	@Qualifier("ZUUL")
	private Bucket4jConfigurationHolder zuulConfigs;
	
	
	
	public Bucket4jEndpoint() {
		super("bucket4j");
	}

	@Override
	public Map<String, Object> invoke() {
		Map<String, Object> result = new HashMap<>();
		if(servletConfigs != null) {
			result.put("servlet", servletConfigs.getFilterConfiguration());
		}
		if(zuulConfigs != null) {
			result.put("zuul", zuulConfigs.getFilterConfiguration());
		}
		
		return result;
	}


}
