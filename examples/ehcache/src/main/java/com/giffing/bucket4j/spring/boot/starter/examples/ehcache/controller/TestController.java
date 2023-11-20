package com.giffing.bucket4j.spring.boot.starter.examples.ehcache.controller;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;

@RestController
@RequestMapping("/")
public class TestController {
//	http.authorizeRequests().antMatchers("/unsecure").permitAll();
//	http.authorizeRequests().antMatchers("/login").permitAll();
//	http.authorizeRequests().antMatchers("/secure").hasAnyRole("ADMIN","USER").

	private final CacheManager<String, Bucket4JConfiguration> manager;

	public TestController(CacheResolver cacheResolver){
		manager = cacheResolver.resolveConfigCacheManager("filterConfigCache");
	}

	@GetMapping("unsecure")
	public ResponseEntity unsecure() {
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("login")
	public ResponseEntity login() {
		
		Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();
        GrantedAuthority grantedAuthority = new GrantedAuthority() {
            //anonymous inner type
            @Override 
            public String getAuthority() {
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


	@GetMapping("hello")
	public ResponseEntity<String> hello() {
		return ResponseEntity.ok("Hello World");
	}

	@PostMapping("filters/{filterId}")
	public ResponseEntity updateConfig(@PathVariable String filterId, @RequestBody Bucket4JConfiguration filter){
		manager.setValue(filterId, filter);
		return ResponseEntity.ok().build();
	}
	
}
