package com.giffing.bucket4j.spring.boot.starter.config.service;

import com.giffing.bucket4j.spring.boot.starter.service.ExpressionService;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;

@Configuration
public class ServiceConfiguration {


    @Bean
    public ExpressionParser expressionParser() {
        SpelParserConfiguration config = new SpelParserConfiguration(
                SpelCompilerMode.IMMEDIATE,
                this.getClass().getClassLoader());
        return new SpelExpressionParser(config);
    }

    @Bean
    ExpressionService expressionService(ExpressionParser expressionParser, ConfigurableBeanFactory beanFactory) {
        return new ExpressionService(expressionParser, beanFactory);
    }

    @Bean
    RateLimitService rateLimitService(ExpressionService expressionService) {
        return new RateLimitService(expressionService);
    }
}
