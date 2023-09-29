package com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.predicate;

import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.Bucket4JAutoConfigurationServletFilter;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter(Bucket4JAutoConfigurationServletFilter.class)
@ConditionalOnBean(Bucket4JAutoConfigurationServletFilter.class)
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
