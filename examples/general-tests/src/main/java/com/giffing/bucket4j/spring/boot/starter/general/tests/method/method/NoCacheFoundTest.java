package com.giffing.bucket4j.spring.boot.starter.general.tests.method.method;

import com.giffing.bucket4j.spring.boot.starter.exception.NoCacheConfiguredException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DirtiesContext
public class NoCacheFoundTest {

    @Test
    public void assert_startup_failure_when_cache_not_configured() {
        SpringApplication springApplication = new SpringApplication(MethodTestApplication.class);
        Properties properties = new Properties();
        properties.put("bucket4j.cache-to-use", "does_not_exist");
        properties.put("bucket4j.methods[0].name", "default");
        properties.put("bucket4j.methods[0].cache-name", "buckets");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].capacity", "5");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].time", "10");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].unit", "seconds");
        properties.put("bucket4j.methods[0].rate-limit.bandwidths[0].refill-speed", "greedy");
        springApplication.setDefaultProperties(properties);

        var noCacheConfiguredException = Assertions.assertThrows(NoCacheConfiguredException.class, springApplication::run);
        assertNotNull(noCacheConfiguredException);
        assertEquals("does_not_exist", noCacheConfiguredException.getCacheToUse());
    }
}
