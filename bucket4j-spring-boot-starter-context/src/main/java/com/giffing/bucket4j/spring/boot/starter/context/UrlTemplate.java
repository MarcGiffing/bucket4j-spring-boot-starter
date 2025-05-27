package com.giffing.bucket4j.spring.boot.starter.context;

/**
 * Defines the type of URL template used for filtering.
 *
 * <ul>
 *   <li><b>REGEX</b> - The URL is interpreted as a regular expression.</li>
 *   <li><b>CUSTOM</b> - The URL is interpreted as a custom template (e.g. Ant-style)
 *       and converted to a regular expression via a {@link com.giffing.bucket4j.spring.boot.starter.context.UrlTemplateMapper}.</li>
 * </ul>
 */
public enum UrlTemplate {
    /**
     * Interpret the URL as a regular expression.
     */
    REGEX,

    /**
     * Interpret the URL as a custom template pattern (e.g. Ant-style),
     * which is then converted to a regex.
     */
    CUSTOM
}
