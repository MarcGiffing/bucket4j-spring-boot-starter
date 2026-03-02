package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.servlet.predicates.ServletHeaderExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.servlet.predicates.ServletMethodPredicate;
import com.giffing.bucket4j.spring.boot.starter.servlet.predicates.ServletPathExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.servlet.predicates.ServletQueryExecutePredicate;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(HttpServletRequest.class)
@AutoConfigureBefore(Bucket4JAutoConfigurationServletFilter.class)
public class ServletRequestExecutePredicateConfiguration {

    @Bean
    ServletHeaderExecutePredicate servletHeaderExecutePredicate() {
        return new ServletHeaderExecutePredicate();
    }

    @Bean
    ServletMethodPredicate servletMethodPredicate() {
        return new ServletMethodPredicate();
    }

    @Bean
    ServletPathExecutePredicate servletPathExecutePredicate() {
        return new ServletPathExecutePredicate();
    }

    @Bean
    ServletQueryExecutePredicate servletQueryExecutePredicate() {
        return new ServletQueryExecutePredicate();
    }

}
