package com.giffing.bucket4j.spring.boot.starter.examples.ehcache.config.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

	public String username() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if(authentication == null) {
			return null;
		}
		String name = authentication.getName();
if(Objects.equals(name, "anonymousUser")) {
			return null;
		}
		return name;
	}
	
}
