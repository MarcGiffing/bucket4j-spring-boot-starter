package com.giffing.bucket4j.spring.boot.starter.zuul

import com.giffing.bucket4j.spring.boot.starter.context.FilterConfiguration
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitConditionMatchingStrategy
import com.netflix.zuul.context.RequestContext
import io.github.bucket4j.ConsumptionProbe
import org.springframework.mock.web.MockHttpServletRequest
import spock.lang.Specification

import static javax.ws.rs.HttpMethod.GET

class ZuulRateLimitFilterSpec extends Specification {

    ZuulRateLimitFilter filter
    FilterConfiguration configuration
    RateLimitCheck rateLimitCheck1
    RateLimitCheck rateLimitCheck2
    RateLimitCheck rateLimitCheck3

    def "setup"() {
        rateLimitCheck1 = Mock(RateLimitCheck)
        rateLimitCheck2 = Mock(RateLimitCheck)
        rateLimitCheck3 = Mock(RateLimitCheck)

        configuration = new FilterConfiguration(url: '/url', rateLimitChecks: [rateLimitCheck1, rateLimitCheck2, rateLimitCheck3])
        filter = new ZuulRateLimitFilter(configuration)
    }

    def "Should execute all checks when using RateLimitConditionMatchingStrategy.ALL"() {
        given: "a valid RequestContext and RateLimitConditionMatchingStrategy.ALL"

            MockHttpServletRequest request = new MockHttpServletRequest(GET, '/url')
            def context = new RequestContext(request: request)
            RequestContext.testSetCurrentContext(context)

            configuration.strategy = RateLimitConditionMatchingStrategy.ALL

        when: "executing the rate limit filter"
            filter.run()

        then: "all rate limit checks are performed"
            1 * rateLimitCheck1.rateLimit(_) >> ConsumptionProbe.consumed(1)
            1 * rateLimitCheck2.rateLimit(_) >> ConsumptionProbe.consumed(1)
            1 * rateLimitCheck3.rateLimit(_) >> ConsumptionProbe.consumed(1)
    }

    def "Should execute all checks when using RateLimitConditionMatchingStrategy.ALL, even if a rate limit check is skipped"() {
        given: "a valid RequestContext and RateLimitConditionMatchingStrategy.ALL"

            MockHttpServletRequest request = new MockHttpServletRequest(GET, '/url')
            def context = new RequestContext(request: request)
            RequestContext.testSetCurrentContext(context)

            configuration.strategy = RateLimitConditionMatchingStrategy.ALL

        when: "executing the rate limit filter"
            filter.run()

        then: "all rate limit checks are performed"
            1 * rateLimitCheck1.rateLimit(_) >> null
            1 * rateLimitCheck2.rateLimit(_) >> ConsumptionProbe.consumed(1)
            1 * rateLimitCheck3.rateLimit(_) >> ConsumptionProbe.consumed(1)
    }

    def "Should only execute first checks when using RateLimitConditionMatchingStrategy.FIRST"() {
        given: "a valid RequestContext and RateLimitConditionMatchingStrategy.ALL"

            MockHttpServletRequest request = new MockHttpServletRequest(GET, '/url')
            def context = new RequestContext(request: request)
            RequestContext.testSetCurrentContext(context)

            configuration.strategy = RateLimitConditionMatchingStrategy.FIRST

        when: "executing the rate limit filter"
            filter.run()

        then: "all rate limit checks are performed"
            1 * rateLimitCheck1.rateLimit(_) >> ConsumptionProbe.consumed(1)
            0 * rateLimitCheck2.rateLimit(_) >> ConsumptionProbe.consumed(1)
            0 * rateLimitCheck3.rateLimit(_) >> ConsumptionProbe.consumed(1)
    }

    def "Should execute checks until one is not skipped when using RateLimitConditionMatchingStrategy.FIRST"() {
        given: "a valid RequestContext and RateLimitConditionMatchingStrategy.ALL"

            MockHttpServletRequest request = new MockHttpServletRequest(GET, '/url')
            def context = new RequestContext(request: request)
            RequestContext.testSetCurrentContext(context)

            configuration.strategy = RateLimitConditionMatchingStrategy.FIRST

        when: "executing the rate limit filter"
            filter.run()

        then: "all rate limit checks are performed"
            1 * rateLimitCheck1.rateLimit(_) >> null
            1 * rateLimitCheck2.rateLimit(_) >> ConsumptionProbe.consumed(1)
            0 * rateLimitCheck3.rateLimit(_)
    }

    private RateLimitCheck mockRateLimitCheck() {
        def limitCheck = Mock(RateLimitCheck)
        limitCheck.rateLimit(_) >> {
            ConsumptionProbe.consumed(1)
        }
        limitCheck
    }

}
