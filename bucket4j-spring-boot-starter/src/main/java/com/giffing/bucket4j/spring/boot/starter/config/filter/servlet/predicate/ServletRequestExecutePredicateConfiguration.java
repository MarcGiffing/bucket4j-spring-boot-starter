package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.predicate;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
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
