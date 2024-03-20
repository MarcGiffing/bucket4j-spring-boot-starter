package com.giffing.bucket4j.spring.boot.starter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.AsyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.cache.Bucket4jCacheConfiguration;
import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.condition.ConditionalOnBucket4jEnabled;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.IgnoreRateLimiting;
import com.giffing.bucket4j.spring.boot.starter.exception.RateLimitUnknownParameterException;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimiting;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.exception.NoCacheConfiguredException;
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
                    // TODO check configuration name exists in properties
                    // TODO use expressions from property if not set in annotation
                    assertValidExpression(rateLimitClass, method, rateLimitingAnnotation.executeCondition());
                    assertValidExpression(rateLimitClass, method, rateLimitingAnnotation.skipCondition());
                    assertValidExpression(rateLimitClass, method, rateLimitingAnnotation.cacheKey());
                    // TODO check fallback method
                }

            }
        }
    }

    private void assertValidExpression(Class<?> rateLimitClass, Method method, String expression) {
        if(StringUtils.hasText(expression)) {
            var executeConditionExpression = (SpelExpression) parser.parseExpression(expression);
            Set<String> parameterInExpression = getParametersFromSpelNode(executeConditionExpression.getAST());
            Set<String> parameterFromMethod = Arrays.stream(method.getParameters()).map(Parameter::getName).collect(Collectors.toSet());
            if(!parameterFromMethod.containsAll(parameterInExpression)) {
                throw new RateLimitUnknownParameterException(expression, rateLimitClass.getName(), method.getName(), parameterFromMethod);
            }
        }
    }

    @NotNull
    private static Set<String> getParametersFromSpelNode(SpelNode spelNode) {
        Set<String> params = new HashSet<>();
        for(int childIndex = 0; childIndex < spelNode.getChildCount(); childIndex++) {
            SpelNode child = spelNode.getChild(childIndex);
            if(child instanceof VariableReference r) {
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