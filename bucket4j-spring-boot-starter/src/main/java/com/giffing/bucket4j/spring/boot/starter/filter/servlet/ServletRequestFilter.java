package com.giffing.bucket4j.spring.boot.starter.filter.servlet;

import com.giffing.bucket4j.spring.boot.starter.context.ExpressionParams;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResult;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Servlet {@link Filter} class to configure Bucket4j on each request.
 */
@Setter
@Slf4j
public class ServletRequestFilter extends OncePerRequestFilter implements Ordered {

    protected String ATTRIBUTE_URL_VARIABLES = "urlVariables";

    private FilterConfiguration<HttpServletRequest, HttpServletResponse> filterConfig;

    public ServletRequestFilter(FilterConfiguration<HttpServletRequest, HttpServletResponse> filterConfig) {
        this.filterConfig = filterConfig;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        var variables =
                filterConfig.getUrlPatternMatcher().matchAndExtract(
                        request.getRequestURI(),
                        request.getQueryString());
        if (variables == null) return true;

        request.setAttribute(ATTRIBUTE_URL_VARIABLES, variables);
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        boolean allConsumed = true;
        Long remainingLimit = null;
        for (var rl : filterConfig.getRateLimitChecks()) {
            var wrapper =
                    rl.rateLimit(
                            new ExpressionParams<>(request)
                                    .addParam("urlPattern", filterConfig.getUrlPattern())
                                    .addParam("urlVariables", request.getAttribute(ATTRIBUTE_URL_VARIABLES)),
                            null);
            if (wrapper != null && wrapper.getRateLimitResult() != null) {
                var rateLimitResult = wrapper.getRateLimitResult();
                if (rateLimitResult.isConsumed()) {
                    remainingLimit = RateLimitService.getRemainingLimit(remainingLimit, rateLimitResult);
                } else {
                    allConsumed = false;
                    handleHttpResponseOnRateLimiting(response, rateLimitResult);
                    break;
                }
                if (filterConfig.getStrategy().equals(RateLimitConditionMatchingStrategy.FIRST)) {
                    break;
                }
            }
        }

        if (allConsumed) {
            if (remainingLimit != null && Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
                log.debug("add-x-rate-limit-remaining-header;limit:{}", remainingLimit);
                response.setHeader("X-Rate-Limit-Remaining", "" + remainingLimit);
            }
            filterChain.doFilter(request, response);
            filterConfig.getPostRateLimitChecks()
                    .forEach(rlc -> {
                        @SuppressWarnings("unchecked")
                        var result =
                                rlc.rateLimit(
                                        request,
                                        response,
                                        new ExpressionParams<>(request)
                                                .addParam("urlPattern", filterConfig.getUrlPattern())
                                                .addParam("urlVariables", request.getAttribute(ATTRIBUTE_URL_VARIABLES)));
                        if (result != null) {
                            log.debug("post-rate-limit;remaining-tokens:{}", result.getRateLimitResult().getRemainingTokens());
                        }
                    });
        }

    }

    private void handleHttpResponseOnRateLimiting(HttpServletResponse httpResponse, RateLimitResult rateLimitResult) throws IOException {
        httpResponse.setStatus(filterConfig.getHttpStatusCode().value());
        if (Boolean.FALSE.equals(filterConfig.getHideHttpResponseHeaders())) {
            httpResponse.setHeader("X-Rate-Limit-Retry-After-Seconds", "" + TimeUnit.NANOSECONDS.toSeconds(rateLimitResult.getNanosToWaitForRefill()));
            filterConfig.getHttpResponseHeaders().forEach(httpResponse::setHeader);
        }
        if (filterConfig.getHttpResponseBody() != null) {
            httpResponse.setContentType(filterConfig.getHttpContentType());
            httpResponse.getWriter().append(filterConfig.getHttpResponseBody());
        }
    }


    @Override
    public int getOrder() {
        return filterConfig.getOrder();
    }
}
