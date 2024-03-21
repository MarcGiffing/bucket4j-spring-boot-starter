package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.IgnoreRateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.properties.MethodProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import com.giffing.bucket4j.spring.boot.starter.exception.*;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.ast.VariableReference;
import org.springframework.expression.spel.standard.SpelExpression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

import static com.giffing.bucket4j.spring.boot.starter.utils.RateLimitAopUtils.getAnnotationFromMethodOrClass;

@Configuration
@ConditionalOnBucket4jEnabled
@AutoConfigureAfter(value = {
        CacheAutoConfiguration.class,
        Bucket4jCacheConfiguration.class
})
@RequiredArgsConstructor
@EnableConfigurationProperties(Bucket4JBootProperties.class)
public class Bucket4jStartupCheckConfiguration {

    private final Bucket4JBootProperties properties;

    @Nullable
    private final SyncCacheResolver syncCacheResolver;

    @Nullable
    private final AsyncCacheResolver asyncCacheResolver;

    private final AbstractApplicationContext context;

    private SpelExpressionParser parser;

    @EventListener
    public void applicationReady(ApplicationReadyEvent event) {
        assertCacheConfiguration();
        assertValidAnnotationConfiguration();
    }

    private void assertValidAnnotationConfiguration() {
        SpelParserConfiguration config = new SpelParserConfiguration(
                SpelCompilerMode.IMMEDIATE,
                this.getClass().getClassLoader());
        parser = new SpelExpressionParser(config);


        var rateLimitingAnnotatedClasses = getRateLimitingAnnotatedClasses();
        for (var rateLimitClass : rateLimitingAnnotatedClasses) {
            for (var method : rateLimitClass.getMethods()) {
                var rateLimitingAnnotation = getAnnotationFromMethodOrClass(method, RateLimiting.class);
                if (rateLimitingAnnotation != null) {

                    assertMethodNameExistsInProperties(rateLimitClass, method, rateLimitingAnnotation);

                    MethodProperties methodProperties = getPropertyFromConfigName(rateLimitingAnnotation.name());
                    RateLimit rateLimit = methodProperties.getRateLimit();

                    String executeConditionExpression = StringUtils.hasText(rateLimitingAnnotation.executeCondition())
                            ? rateLimitingAnnotation.executeCondition() : rateLimit.getExecuteCondition();
                    assertValidExpression(rateLimitClass, method, executeConditionExpression);

                    String skipConditionExpression = StringUtils.hasText(rateLimitingAnnotation.skipCondition())
                            ? rateLimitingAnnotation.skipCondition() : rateLimit.getSkipCondition();
                    assertValidExpression(rateLimitClass, method, skipConditionExpression);

                    String cacheKeyExpression = StringUtils.hasText(rateLimitingAnnotation.cacheKey())
                            ? rateLimitingAnnotation.cacheKey() : rateLimit.getCacheKey();
                    assertValidExpression(rateLimitClass, method, cacheKeyExpression);

                    assertValidFallbackMethod(rateLimitClass, method, rateLimitingAnnotation);
                }
            }
        }
    }

    private static void assertValidFallbackMethod(Class<?> rateLimitClass, Method method, RateLimiting rateLimitingAnnotation) {
        var fallbackMethodName = rateLimitingAnnotation.fallbackMethodName();
        if (StringUtils.hasText(fallbackMethodName)) {
            var fallbackMethods = Arrays.stream(method.getDeclaringClass().getMethods())
                    .filter(p -> p.getName().equals(fallbackMethodName))
                    .toList();
            if (fallbackMethods.isEmpty()) {
                throw new RateLimitingFallbackMethodNotFoundException(fallbackMethodName, rateLimitClass.getName(), method.getName());
            }
            if (fallbackMethods.size() > 1) {
                throw new RateLimitingMultipleFallbackMethodsFoundException(fallbackMethodName, rateLimitClass.getName(), method.getName());
            }

            var fallbackMethod = fallbackMethods.get(0);

            if (!method.getReturnType().equals(fallbackMethod.getReturnType())) {
                throw new RateLimitingFallbackReturnTypesMismatchException(
                        fallbackMethodName,
                        rateLimitClass.getName(),
                        method.getName(),
                        method.getReturnType().toGenericString(),
                        fallbackMethod.getReturnType().toGenericString());
            }


            if (!Arrays.equals(method.getParameterTypes(), fallbackMethod.getParameterTypes())) {
                throw new RateLimitingFallbackMethodParameterMismatchException(fallbackMethodName, rateLimitClass.getName(), method.getName(),
                        getParametersAsString(method), getParametersAsString(fallbackMethod));
            }

        }
    }

    @NotNull
    private static String getParametersAsString(Method method) {
        return Arrays.stream(method.getParameters())
                .map(p -> p.getName() + ":" + p.getType())
                .collect(Collectors.joining(";"));
    }

    private MethodProperties getPropertyFromConfigName(String configName) {
        return properties.getMethods()
                .stream()
                .filter(mc -> mc.getName().equals(configName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Could not find config with name " + configName));
    }

    private void assertMethodNameExistsInProperties(Class<?> rateLimitClass, Method method, RateLimiting rateLimitingAnnotation) {
        var methodConfigNames = properties.getMethods().stream().map(MethodProperties::getName).collect(Collectors.toSet());
        if (!methodConfigNames.contains(rateLimitingAnnotation.name())) {
            throw new RateLimitingMethodNameNotConfiguredException(rateLimitingAnnotation.name(), methodConfigNames, rateLimitClass.getName(), method.getName());
        }
    }

    private void assertValidExpression(Class<?> rateLimitClass, Method method, String expression) {
        if (StringUtils.hasText(expression)) {
            var executeConditionExpression = (SpelExpression) parser.parseExpression(expression);
            Set<String> parameterInExpression = getParametersFromSpelNode(executeConditionExpression.getAST());
            Set<String> parameterFromMethod = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toSet());
            if (!parameterFromMethod.containsAll(parameterInExpression)) {
                throw new RateLimitUnknownParameterException(expression, rateLimitClass.getName(), method.getName(), parameterFromMethod);
            }
        }
    }

    @NotNull
    private static Set<String> getParametersFromSpelNode(SpelNode spelNode) {
        Set<String> params = new HashSet<>();
        if (spelNode instanceof VariableReference r) {
            params.add(r.toStringAST().substring(1));
        }
        for (int childIndex = 0; childIndex < spelNode.getChildCount(); childIndex++) {
            SpelNode child = spelNode.getChild(childIndex);
            if (child instanceof VariableReference r) {
                params.add(r.toStringAST().substring(1));
            }
            params.addAll(getParametersFromSpelNode(child));

        }
        return params;
    }

    private List<? extends Class<?>> getRateLimitingAnnotatedClasses() {
        return Arrays.stream(context.getBeanDefinitionNames())
                .map(context::getBean)
                .map(AopUtils::getTargetClass)
                .filter(targetClass -> {
                    boolean excludeClassesAnnotatedWithIgnoreRateLimiting = !targetClass.isAnnotationPresent(IgnoreRateLimiting.class);
                    boolean methodsWhereTheClassIsAnnotatedWithRateLimiting = targetClass.isAnnotationPresent(RateLimiting.class);
                    boolean methodsThatAreAnnotatedWithRateLimitingButWithoutIgnoringRateLimiting = Arrays
                            .stream(targetClass.getMethods())
                            .anyMatch(m -> m.isAnnotationPresent(RateLimiting.class) && !m.isAnnotationPresent(IgnoreRateLimiting.class));
                    return
                            excludeClassesAnnotatedWithIgnoreRateLimiting && (
                                    methodsWhereTheClassIsAnnotatedWithRateLimiting || methodsThatAreAnnotatedWithRateLimitingButWithoutIgnoringRateLimiting
                            );
                }).toList();
    }

    private void assertCacheConfiguration() {
        // Either a sync or an async cache resolver must be provided
        if (syncCacheResolver == null && asyncCacheResolver == null) {
            throw new NoCacheConfiguredException(properties.getCacheToUse());
        }

        var filterMethods = getAllFilterMethods();
        checkCacheResolverForFiltersIsRequired(syncCacheResolver == null, filterMethods, Set.of(FilterMethod.SERVLET));
        checkCacheResolverForFiltersIsRequired(asyncCacheResolver == null, filterMethods, Set.of(FilterMethod.WEBFLUX, FilterMethod.GATEWAY));
    }

    private Set<FilterMethod> getAllFilterMethods() {
        return properties.getFilters()
                .stream()
                .map(Bucket4JConfiguration::getFilterMethod)
                .collect(Collectors.toSet());
    }

    private void checkCacheResolverForFiltersIsRequired(boolean cacheResolverNotExists, Set<FilterMethod> filterMethodsToCheck, Set<FilterMethod> configuredFilterMethods) {
        var filterFound = configuredFilterMethods.stream().anyMatch(filterMethodsToCheck::contains);
        // if a servlet filter is configured but no syncCacheResolver found
        if (filterFound && cacheResolverNotExists) {
            throw new NoCacheConfiguredException(properties.getCacheToUse());
        }
    }
}