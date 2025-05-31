package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.UrlMapper;
import com.giffing.bucket4j.spring.boot.starter.url.RegexUrlMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBucket4jEnabled
public class Bucket4jUrlConfiguration {

    @Bean
    @ConditionalOnMissingBean(UrlMapper.class)
    public UrlMapper urlMapper() {
        return new RegexUrlMapper();
    }

}
