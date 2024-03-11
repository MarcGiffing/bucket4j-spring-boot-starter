package com.giffing.bucket4j.spring.boot.starter.config.aspect;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.*;
import com.giffing.bucket4j.spring.boot.starter.context.properties.MethodProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Metrics;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.service.RateLimitService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitAspect {

    private final RateLimitService rateLimitService;

    private final List<MethodProperties> methodProperties;

    private final SyncCacheResolver syncCacheResolver;

    private Map<String, RateLimitService.RateLimitConfigresult<Method, Object>> rateLimitConfigResults = new HashMap<>();

    @PostConstruct
    public void init() {
        for(var methodProperty : methodProperties) {
            var proxyManagerWrapper = syncCacheResolver.resolve(methodProperty.getCacheName());
            var rateLimitConfig = RateLimitService.RateLimitConfig.<Method>builder()
                    .rateLimits(List.of(methodProperty.getRateLimit()))
                    .metricHandlers(List.of())
                    .executePredicates(Map.of())
                    .cacheName(methodProperty.getCacheName())
                    .configVersion(0)
                    .keyFunction((rl, sr) -> {
                        KeyFilter<Method> keyFilter = rateLimitService.getKeyFilter(sr.getRootObject().getName(), rl);
                        return keyFilter.key(sr);
                    })
                    .metrics(new Metrics())
                    .proxyWrapper(proxyManagerWrapper)
                    .build();
            var rateLimitConfigResult = rateLimitService.configureRateLimit(rateLimitConfig);
            rateLimitConfigResults.put(methodProperty.getName(), rateLimitConfigResult);
        }
    }

    @Pointcut("@annotation(com.giffing.bucket4j.spring.boot.starter.context.RateLimiting)")
    private void methodsAnnotatedWithRateLimitAnnotation() {
    }

    @Around("methodsAnnotatedWithRateLimitAnnotation()")
    public Object processMethodsAnnotatedWithRateLimitAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimiting rateLimitAnnotation = method.getAnnotation(RateLimiting.class);

        Method fallbackMethod = null;
        if(rateLimitAnnotation.fallbackMethodName() != null) {
            var fallbackMethods = Arrays.stream(method.getDeclaringClass().getMethods())
                    .filter(p -> p.getName().equals(rateLimitAnnotation.fallbackMethodName()))
                    .toList();
            if(fallbackMethods.size() > 1) {
                throw new IllegalStateException("Found " + fallbackMethods.size() + " fallbackMethods for " + rateLimitAnnotation.fallbackMethodName());
            }
            if(!fallbackMethods.isEmpty()) {
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
        } else if (fallbackMethod != null){
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
        if(allConsumed) {
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
        if(!rateLimitConfigResults.containsKey(rateLimitAnnotation.name())) {
            throw new IllegalStateException("Could not find cache " + rateLimitAnnotation.name());
        }
    }

    private static Map<String, Object> collectExpressionParameter(Object[] args, String[] parameterNames) {
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i< args.length; i++) {
            log.debug("expresion-params;name:{};arg:{}", parameterNames[i], args[i]);
            params.put(parameterNames[i], args[i]);
        }
        return params;
    }




}
