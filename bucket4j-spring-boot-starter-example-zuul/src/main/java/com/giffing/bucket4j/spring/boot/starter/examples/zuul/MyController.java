package com.giffing.bucket4j.spring.boot.starter.examples.zuul;

import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableZuulProxy
@RequestMapping
public class MyController {

	@RequestMapping
	  public String available() {
	    return "Spring in Action";
	  }
	
}
