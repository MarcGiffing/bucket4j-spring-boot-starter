package com.giffing.bucket4j.spring.boot.starter.examples.ehcache.config.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

	public String username() {
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if(name == "anonymousUser") {
			return null;
		}
		return name;
	}
	
}
