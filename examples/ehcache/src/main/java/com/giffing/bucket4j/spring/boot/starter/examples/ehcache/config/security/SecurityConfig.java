package com.giffing.bucket4j.spring.boot.starter.examples.ehcache.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests().requestMatchers("/unsecure").permitAll();
		http.authorizeHttpRequests().requestMatchers("/login").permitAll();
		http.authorizeHttpRequests().requestMatchers("/secure").hasAnyRole("ADMIN", "USER");
		return http.build();
	}

	@Bean
	public UserDetailsService inMemoryUser() throws Exception {
		UserDetails user = User.builder()
				.username("admin")
				.password("123")
				.roles("ADMIN")
				.build();
		return new InMemoryUserDetailsManager(user);
	}
}
