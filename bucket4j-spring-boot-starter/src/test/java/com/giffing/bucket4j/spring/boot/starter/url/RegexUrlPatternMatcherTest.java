package com.giffing.bucket4j.spring.boot.starter.url;

import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegexUrlPatternMatcherTest {

    @Test
    void should_match_when_path_matches() {
        var matcher =
                RegexUrlPatternMatcher.create("/api/.*");

        assertTrue(matcher.matches("/api/test", null));
        assertFalse(matcher.matches("/admin/test", null));
    }

    @Test
    void should_extract_single_named_group() {
        UrlPatternMatcher matcher =
                RegexUrlPatternMatcher.create(
                        "/api/v1/(?<resource>\\w+)");
        assertTrue(matcher.matches("/api/v1/model", null));

        var extracted =
                matcher.matchAndExtract("/api/v1/model", null);
        assertNotNull(extracted);
        assertEquals(1, extracted.size());
        assertEquals("model", extracted.get("resource"));
    }

    @Test
    void should_return_null_when_path_not_matched() {
        var matcher =
                RegexUrlPatternMatcher.create("/api/(?<id>[0-9]+)");

        assertNull(matcher.matchAndExtract("/other/123", null));
    }

    @Test
    void should_extract_multiple_named_groups() {
        var matcher =
                RegexUrlPatternMatcher.create(
                        "/(?<entity>\\w+)/(?<id>\\d+)");

        var extracted =
                matcher.matchAndExtract("/user/42", null);
        assertNotNull(extracted);
        assertEquals(2, extracted.size());
        assertEquals("user", extracted.get("entity"));
        assertEquals("42", extracted.get("id"));
    }

    @Test
    void should_return_empty_map_when_no_groups() {
        var matcher =
                RegexUrlPatternMatcher.create("/api/.*");
        var extracted =
                matcher.matchAndExtract("/api/test", null);
        assertTrue(extracted.isEmpty());
    }
}
