package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParser;
import com.giffing.bucket4j.spring.boot.starter.url.PathPatternUrlPatternParser;
import com.giffing.bucket4j.spring.boot.starter.url.RegexUrlPatternParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBucket4jEnabled
public class Bucket4jUrlPatternConfiguration {

    @Bean
    @ConditionalOnProperty(
            name = "bucket4j.url-pattern-parser",
            havingValue = "regex",
            matchIfMissing = true)
    public UrlPatternParser regexUrlPatternParser() {
        return new RegexUrlPatternParser();
    }

    @Bean
    @ConditionalOnProperty(
            name = "bucket4j.url-pattern-parser",
            havingValue = "path-pattern")
    public UrlPatternParser pathPatternUrlPatternParser() {
        return new PathPatternUrlPatternParser();
    }

}
