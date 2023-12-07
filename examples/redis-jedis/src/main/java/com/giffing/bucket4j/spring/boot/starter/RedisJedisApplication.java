package com.giffing.bucket4j.spring.boot.starter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.jmx.support.RegistrationPolicy;

@SpringBootApplication
@EnableMBeanExport(registration=RegistrationPolicy.IGNORE_EXISTING)
public class RedisJedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedisJedisApplication.class, args);
    }

}