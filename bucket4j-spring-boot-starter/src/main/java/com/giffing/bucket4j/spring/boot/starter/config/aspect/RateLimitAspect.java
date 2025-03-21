package com.giffing.bucket4j.spring.boot.starter.config.aspect;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.*;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Metrics;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import com.giffing.bucket4j.spring.boot.starter.utils.RateLimitAopUtils;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Creates an Aspect around methods annotated with @{@link RateLimiting}. It prevents the execution of the method
 * if rate limit should be executed.
 */
@Aspect
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    private final Bucket4JBootProperties bucket4JBootProperties;

    private final SyncCacheResolver syncCacheResolver;

    private final List<MetricHandler> metricHandlers;

    private final Map<String, RateLimitService.RateLimitConfigresult<Method, Object>> rateLimitConfigResults = new HashMap<>();

    @PostConstruct
    public void init() {
        for (var methodProperty : bucket4JBootProperties.getMethods()) {
            var proxyManagerWrapper = syncCacheResolver.resolve(methodProperty.getCacheName());
            var rateLimitConfig = RateLimitService.RateLimitConfig.<Method>builder()
                    .rateLimits(List.of(methodProperty.getRateLimit()))
                    .metricHandlers(metricHandlers)
                    .executePredicates(Map.of())
                    .cacheName(methodProperty.getCacheName())
                    .configVersion(0)
                    .keyFunction((rl, sr) -> {
                        KeyFilter<Method> keyFilter = rateLimitService.getKeyFilter(sr.getRootObject().getName(), rl);
                        return keyFilter.key(sr);
                    })
                    .metrics(new Metrics(bucket4JBootProperties.getDefaultMethodMetricTags()))
                    .proxyWrapper(proxyManagerWrapper)
                    .build();
            var rateLimitConfigResult = rateLimitService.configureRateLimit(rateLimitConfig);
            rateLimitConfigResults.put(methodProperty.getName(), rateLimitConfigResult);
        }
    }

    @Pointcut("execution(public * *(..))")
    public void publicMethod() {
    }

    @Pointcut("@annotation(com.giffing.bucket4j.spring.boot.starter.context.RateLimiting)")
    private void methodsAnnotatedWithRateLimitAnnotation() {
    }

    @Pointcut("@within(com.giffing.bucket4j.spring.boot.starter.context.RateLimiting) && publicMethod()")
    private void classAnnotatedWithRateLimitAnnotation() {

    }

    @Around("methodsAnnotatedWithRateLimitAnnotation() || classAnnotatedWithRateLimitAnnotation()")
    public Object processMethodsAnnotatedWithRateLimitAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        var ignoreRateLimitAnnotation = RateLimitAopUtils.getAnnotationFromMethodOrClass(method, IgnoreRateLimiting.class);
        // if the class or method is annotated with IgnoreRateLimiting we will skip rate limiting
        if (ignoreRateLimitAnnotation != null) {
            return joinPoint.proceed();
        }

        var rateLimitAnnotation = RateLimitAopUtils.getAnnotationFromMethodOrClass(method, RateLimiting.class);

        Method fallbackMethod = null;
        if(StringUtils.hasText(rateLimitAnnotation.fallbackMethodName())) {

            var fallbackMethods = Arrays.stream(method.getDeclaringClass().getMethods())
                    .filter(p -> p.getName().equals(rateLimitAnnotation.fallbackMethodName()))
                    .toList();
            if (fallbackMethods.size() > 1) {
                throw new IllegalStateException("Found " + fallbackMethods.size() + " fallbackMethods for " + rateLimitAnnotation.fallbackMethodName());
            }
            if (!fallbackMethods.isEmpty()) {
                fallbackMethod = joinPoint.getTarget().getClass().getMethod(rateLimitAnnotation.fallbackMethodName(), ((MethodSignature) joinPoint.getSignature()).getParameterTypes());
            }
        }

        Map<String, Object> params = collectExpressionParameter(
                joinPoint.getArgs(),
                signature.getParameterNames());

        assertValidCacheName(rateLimitAnnotation);

        var annotationRateLimit = buildMainRateLimitConfiguration(rateLimitAnnotation);
        var rateLimitConfigResult = rateLimitConfigResults.get(rateLimitAnnotation.name());

        RateLimitConsumedResult consumedResult = performRateLimit(rateLimitConfigResult, method, params, annotationRateLimit);

        Object methodResult;

        if (consumedResult.allConsumed()) {
            // no rate limit - execute the surrounding method
            methodResult = joinPoint.proceed();
            performPostRateLimit(rateLimitConfigResult, method, methodResult);
        } else if (fallbackMethod != null) {
            return fallbackMethod.invoke(joinPoint.getTarget(), joinPoint.getArgs());
        } else {
            throw new RateLimitException();
        }

        return methodResult;
    }


    private static void performPostRateLimit(RateLimitService.RateLimitConfigresult<Method, Object> rateLimitConfigResult, Method method, Object methodResult) {
        for (var rlc : rateLimitConfigResult.getPostRateLimitChecks()) {
            var result = rlc.rateLimit(method, methodResult);
            if (result != null) {
                log.debug("post-rate-limit;remaining-tokens:{}", result.getRateLimitResult().getRemainingTokens());
            }
        }
    }

    private static RateLimitConsumedResult performRateLimit(RateLimitService.RateLimitConfigresult<Method, Object> rateLimitConfigResult, Method method, Map<String, Object> params, RateLimit annotationRateLimit) {
        boolean allConsumed = true;
        Long remainingLimit = null;
        for (RateLimitCheck<Method> rl : rateLimitConfigResult.getRateLimitChecks()) {
            var wrapper = rl.rateLimit(new ExpressionParams<>(method).addParams(params), annotationRateLimit);
            if (wrapper != null && wrapper.getRateLimitResult() != null) {
                var rateLimitResult = wrapper.getRateLimitResult();
                if (rateLimitResult.isConsumed()) {
                    remainingLimit = RateLimitService.getRemainingLimit(remainingLimit, rateLimitResult);
                } else {
                    allConsumed = false;
                    break;
                }
            }
        }
        if (allConsumed) {
            log.debug("rate-limit-remaining;limit:{}", remainingLimit);
        }
        return new RateLimitConsumedResult(allConsumed, remainingLimit);
    }

    private record RateLimitConsumedResult(boolean allConsumed, Long remainingLimit) {
    }

    /*
     * Uses the configuration of the annotation to crate a main RateLimit which overrides
     * the configuration from the property files.
     */
    private static RateLimit buildMainRateLimitConfiguration(RateLimiting rateLimitAnnotation) {
        var annotationRateLimit = new RateLimit();
        annotationRateLimit.setExecuteCondition(rateLimitAnnotation.executeCondition());
        annotationRateLimit.setCacheKey(rateLimitAnnotation.cacheKey());
        annotationRateLimit.setSkipCondition(rateLimitAnnotation.skipCondition());
        return annotationRateLimit;
    }

    private void assertValidCacheName(RateLimiting rateLimitAnnotation) {
        if (!rateLimitConfigResults.containsKey(rateLimitAnnotation.name())) {
            throw new IllegalStateException("Could not find cache " + rateLimitAnnotation.name());
        }
    }

    private static Map<String, Object> collectExpressionParameter(Object[] args, String[] parameterNames) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            log.debug("expresion-params;name:{};arg:{}", parameterNames[i], args[i]);
            params.put(parameterNames[i], args[i]);
        }
        return params;
    }


}
