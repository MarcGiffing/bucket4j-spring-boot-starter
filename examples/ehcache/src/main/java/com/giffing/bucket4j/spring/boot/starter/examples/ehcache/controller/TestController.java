package com.giffing.bucket4j.spring.boot.starter.examples.ehcache.controller;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class TestController {
//	http.authorizeRequests().antMatchers("/unsecure").permitAll();
//	http.authorizeRequests().antMatchers("/login").permitAll();
//	http.authorizeRequests().antMatchers("/secure").hasAnyRole("ADMIN","USER").
	
	@GetMapping("unsecure")
	public ResponseEntity unsecure() {
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("login")
	public ResponseEntity login() {
		
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            //anonymous inner type
            @Override public String getAuthority() {
                return "ROLE_USER";
            }
        }; 
        grantedAuthorities.add(grantedAuthority);
		
		Authentication auth =  new UsernamePasswordAuthenticationToken(new User("admin"), null, grantedAuthorities);
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("secure")
	public ResponseEntity secure() {
		return ResponseEntity.ok().build();
	}
	
	public static class User {
		public String username;
		
		public User(String username) {
			this.username = username;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		@Override
		public String toString() {
			return username;
		}
		
	}
	
}
