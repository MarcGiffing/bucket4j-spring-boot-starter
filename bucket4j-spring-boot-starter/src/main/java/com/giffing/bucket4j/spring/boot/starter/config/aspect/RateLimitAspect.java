package com.giffing.bucket4j.spring.boot.starter.config.aspect;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitResult;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
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
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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

    private Map<String, RateLimitService.RateLimitConfigresult<Void, Object>> rateLimitConfigResults = new HashMap<>();

    @PostConstruct
    public void init() {
        for(var methodProperty : methodProperties) {
            var proxyManagerWrapper = syncCacheResolver.resolve(methodProperty.getCacheName());
            var rateLimitConfig = RateLimitService.RateLimitConfig.<Void>builder()
                    .rateLimits(List.of(methodProperty.getRateLimit()))
                    .metricHandlers(List.of())
                    .executePredicates(Map.of())
                    .cacheName(methodProperty.getCacheName())
                    .configVersion(0)
                    .keyFunction((rl, sr) -> rateLimitService.getKeyFilter(methodProperty.getName(), rl).key(sr))
                    .metrics(new Metrics())
                    .proxyWrapper(proxyManagerWrapper)
                    .build();
            var rateLimitConfigResult = rateLimitService.configureRateLimit(rateLimitConfig);
            rateLimitConfigResults.put(methodProperty.getName(), rateLimitConfigResult);
        }
    }

    @Around("methodsAnnotatedWithRateLimitAnnotation()")
    public Object processMethodsAnnotatedWithRateLimitAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        var args = joinPoint.getArgs();
        var parameterNames = signature.getParameterNames();

        var evaluationContext = new StandardEvaluationContext();
        for (int i = 0; i< args.length; i++) {
            log.debug("expresion-params;name:{};arg:{}",parameterNames[i], args[i]);
            evaluationContext.setVariable(parameterNames[i], args[i]);

        }

        RateLimiting rateLimitAnnotation = method.getAnnotation(RateLimiting.class);


        if(!rateLimitConfigResults.containsKey(rateLimitAnnotation.name())) {
            throw new IllegalStateException("Could not find cache " + rateLimitAnnotation.name());
        }
        var rateLimitConfigResult = rateLimitConfigResults.get(rateLimitAnnotation.name());

        var annotationRateLimit = new RateLimit();
        annotationRateLimit.setExecuteCondition(rateLimitAnnotation.executeCondition());
        annotationRateLimit.setCacheKey(rateLimitAnnotation.cacheKey());
        annotationRateLimit.setSkipCondition(rateLimitAnnotation.skipCondition());


        boolean allConsumed = true;
        Long remainingLimit = null;
        for (RateLimitCheck<Void> rl : rateLimitConfigResult.getRateLimitChecks()) {
            var wrapper = rl.rateLimit(null, annotationRateLimit);
            if (wrapper != null && wrapper.getRateLimitResult() != null) {
                var rateLimitResult = wrapper.getRateLimitResult();
                if (rateLimitResult.isConsumed()) {
                    remainingLimit = getRemainingLimit(remainingLimit, rateLimitResult);
                } else {
                    log.debug("rate-limit!");
                    allConsumed = false;
                    break;
                }
            }
        }

        Object methodResult;
        if (allConsumed) {
            if (remainingLimit != null) {
                log.debug("rate-limit-remaining-header;limit:{}", remainingLimit);
            }

            methodResult = joinPoint.proceed();

            for (var rlc : rateLimitConfigResult.getPostRateLimitChecks()) {
                var result = rlc.rateLimit(null, methodResult);
                if (result != null) {
                    log.debug("post-rate-limit;remaining-tokens:{}", result.getRateLimitResult().getRemainingTokens());
                }
            }
        } else {
            throw new RateLimitException();
        }

        return methodResult;
    }
    @Pointcut("@annotation(com.giffing.bucket4j.spring.boot.starter.context.RateLimiting)")
    private void methodsAnnotatedWithRateLimitAnnotation() {

    }

    private long getRemainingLimit(Long remaining, RateLimitResult rateLimitResult) {
        if (rateLimitResult != null) {
            if (remaining == null) {
                remaining = rateLimitResult.getRemainingTokens();
            } else if (rateLimitResult.getRemainingTokens() < remaining) {
                remaining = rateLimitResult.getRemainingTokens();
            }
        }
        return remaining;
    }
}
