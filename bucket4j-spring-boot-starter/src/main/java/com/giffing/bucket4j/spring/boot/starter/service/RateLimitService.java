package com.giffing.bucket4j.spring.boot.starter.service;


import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.context.*;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricBucketListener;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.properties.*;
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateInstantiationException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class RateLimitService {

    private final ExpressionService expressionService;

    @Builder
    @Data
    public static class RateLimitConfig<R> {
        @NonNull
        private List<RateLimit> rateLimits;
        @NonNull
        private List<MetricHandler> metricHandlers;
        @NonNull
        private Map<String, ExecutePredicate<R>> executePredicates;
        @NonNull
        private String cacheName;
        @NonNull
        private ProxyManagerWrapper proxyWrapper;
        @NonNull
        private BiFunction<RateLimit, ExpressionParams<R>, String> keyFunction;
        @NonNull
        private Metrics metrics;
        private long configVersion;
    }

    @Builder
    @Data
    public static class RateLimitConfigresult<R, P> {
        private List<RateLimitCheck<R>> rateLimitChecks;
        private List<PostRateLimitCheck<R, P>> postRateLimitChecks;
    }

    public <R, P> RateLimitConfigresult<R, P> configureRateLimit(RateLimitConfig<R> rateLimitConfig) {


        var executePredicates = rateLimitConfig.getExecutePredicates();
        

        List<RateLimitCheck<R>> rateLimitChecks = new ArrayList<>();
        List<PostRateLimitCheck<R, P>> postRateLimitChecks = new ArrayList<>();
        rateLimitConfig.getRateLimits().forEach(rl -> {
            log.debug("RL: {}", rl.toString());
            var bucketConfiguration = prepareBucket4jConfigurationBuilder(rl).build();
            var executionPredicate = prepareExecutionPredicates(rl, executePredicates);
            var skipPredicate = prepareSkipPredicates(rl, executePredicates);

            RateLimitCheck<R> rlc = (expressionParams, overridableRateLimit) -> {

                var rlToUse = rl.copy();
                rlToUse.consumeNotNullValues(overridableRateLimit);

                var skipRateLimit = performSkipRateLimitCheck(rlToUse, executionPredicate, skipPredicate, expressionParams);
                boolean isEstimation = rlToUse.getPostExecuteCondition() != null;
                RateLimitResultWrapper rateLimitResultWrapper = null;
                if (!skipRateLimit) {

                    rateLimitResultWrapper = tryConsume(rateLimitConfig, expressionParams, rlToUse, isEstimation, bucketConfiguration);
                }
                return rateLimitResultWrapper;
            };
            rateLimitChecks.add(rlc);


            if (rl.getPostExecuteCondition() != null) {
                log.debug("PRL: {}", rl);
                PostRateLimitCheck<R, P> postRlc =
                        (request, response, parameters) -> {
                            var skipRateLimit =
                                    performPostSkipRateLimitCheck(rl,
                                            executionPredicate,
                                            skipPredicate,
                                            parameters,
                                            response);
                            boolean isEstimation = false;
                            RateLimitResultWrapper rateLimitResultWrapper = null;
                            if (!skipRateLimit) {
                                rateLimitResultWrapper =
                                        tryConsume(rateLimitConfig, parameters, rl, isEstimation, bucketConfiguration);
                            }
                            return rateLimitResultWrapper;
                        };
                postRateLimitChecks.add(postRlc);

            }
        });

        return new RateLimitConfigresult<>(rateLimitChecks, postRateLimitChecks);
    }

    private <R> RateLimitResultWrapper tryConsume(RateLimitConfig<R> rateLimitConfig, ExpressionParams<R> expressionParams, RateLimit rlToUse, boolean isEstimation, BucketConfiguration bucketConfiguration) {
        RateLimitResultWrapper rateLimitResultWrapper;
        var metricHandlers = rateLimitConfig.getMetricHandlers();
        var cacheName = rateLimitConfig.getCacheName();
        var metrics = rateLimitConfig.getMetrics();
        var keyFunction = rateLimitConfig.getKeyFunction();
        var proxyWrapper = rateLimitConfig.getProxyWrapper();
        var configVersion = rateLimitConfig.getConfigVersion();

        var key = keyFunction.apply(rlToUse, expressionParams);
        var metricBucketListener = createMetricListener(cacheName, metrics, metricHandlers, expressionParams);
        log.debug("try-and-consume;key:{};tokens:{}", key, rlToUse.getNumTokens());
        rateLimitResultWrapper = proxyWrapper.tryConsumeAndReturnRemaining(
                key,
                rlToUse.getNumTokens(),
                isEstimation,
                bucketConfiguration,
                metricBucketListener,
                configVersion,
                rlToUse.getTokensInheritanceStrategy()
        );
        return rateLimitResultWrapper;
    }


    private <R, P> boolean performPostSkipRateLimitCheck(RateLimit rl,
                                                         Predicate<R> executionPredicate,
                                                         Predicate<R> skipPredicate,
                                                         ExpressionParams<R> expressionParams,
                                                         P response
    ) {
        var skipRateLimit = performSkipRateLimitCheck(
                rl, executionPredicate,
                skipPredicate, expressionParams);

        if (!skipRateLimit && rl.getPostExecuteCondition() != null) {
            Condition<P> condition = exp -> expressionService.parseBoolean(rl.getPostExecuteCondition(), exp);
            skipRateLimit = !condition.evaluate(new ExpressionParams<>(response).addParams(expressionParams.getParams()));
            log.debug("skip-rate-limit - post-execute-condition: {}", skipRateLimit);
        }

        return skipRateLimit;
    }

    private <R> boolean performSkipRateLimitCheck(RateLimit rl,
                                                  Predicate<R> executionPredicate,
                                                  Predicate<R> skipPredicate,
                                                  ExpressionParams<R> expressionParams) {
        boolean skipRateLimit = false;
        if (rl.getSkipCondition() != null) {
            Condition<R> expresison = exp -> expressionService.parseBoolean(rl.getSkipCondition(), exp);
            skipRateLimit = expresison.evaluate(expressionParams);
            log.debug("skip-rate-limit - skip-condition: {}", skipRateLimit);
        }

        if (!skipRateLimit) {
            skipRateLimit = skipPredicate.test(expressionParams.getRootObject());
            log.debug("skip-rate-limit - skip-predicates: {}", skipRateLimit);
        }

        if (!skipRateLimit && rl.getExecuteCondition() != null) {
            Condition<R> condition = exp -> expressionService.parseBoolean(rl.getExecuteCondition(), exp);
            skipRateLimit = !condition.evaluate(expressionParams);
            log.debug("skip-rate-limit - execute-condition: {}", skipRateLimit);
        }

        if (!skipRateLimit) {
            skipRateLimit = !executionPredicate.test(expressionParams.getRootObject());
            log.debug("skip-rate-limit - execute-predicates: {}", skipRateLimit);
        }
        return skipRateLimit;
    }

    public <R> List<MetricTagResult> getMetricTagResults(ExpressionParams<R> expressionParams, Metrics metrics) {
        return metrics
                .getTags()
                .stream()
                .map(metricMetaTag -> {
                    var value = expressionService.parseString(metricMetaTag.getExpression(), expressionParams);
                    return new MetricTagResult(metricMetaTag.getKey(), value, metricMetaTag.getTypes());
                }).toList();
    }

    /**
     * Creates the key filter lambda which is responsible to decide how the rate limit will be performed. The key
     * is the unique identifier like an IP address or a username.
     *
     * @param url       is used to generated a unique cache key
     * @param rateLimit the {@link RateLimit} configuration which holds the skip condition string
     * @return should not been null. If no filter key type is matching a plain 1 is returned so that all requests uses the same key.
     */
    public <R> KeyFilter<R> getKeyFilter(String url, RateLimit rateLimit) {
        return expressionParams -> {
            String value = expressionService.parseString(rateLimit.getCacheKey(), expressionParams);
            return url + "-" + value;
        };
    }


    private ConfigurationBuilder prepareBucket4jConfigurationBuilder(RateLimit rl) {
        var configBuilder = BucketConfiguration.builder();
        for (BandWidth bandWidth : rl.getBandwidths()) {
            long capacity = bandWidth.getCapacity();
            long refillCapacity = bandWidth.getRefillCapacity() != null ? bandWidth.getRefillCapacity() : bandWidth.getCapacity();
            var refillPeriod = Duration.of(bandWidth.getTime(), bandWidth.getUnit());
            var bucket4jBandWidth = switch (bandWidth.getRefillSpeed()) {
                case GREEDY ->
                        Bandwidth.builder().capacity(capacity).refillGreedy(refillCapacity, refillPeriod).id(bandWidth.getId());
                case INTERVAL ->
                        Bandwidth.builder().capacity(capacity).refillIntervally(refillCapacity, refillPeriod).id(bandWidth.getId());
            };

            if (bandWidth.getInitialCapacity() != null) {
                bucket4jBandWidth = bucket4jBandWidth.initialTokens(bandWidth.getInitialCapacity());
            }
            configBuilder = configBuilder.addLimit(bucket4jBandWidth.build());
        }
        return configBuilder;
    }

    private <R> MetricBucketListener createMetricListener(String cacheName,
                                                          Metrics metrics,
                                                          List<MetricHandler> metricHandlers,
                                                          ExpressionParams<R> expressionParams) {

        var metricTagResults = getMetricTags(metrics, expressionParams);
        return new MetricBucketListener(
                cacheName,
                metricHandlers,
                metrics.getTypes(),
                metricTagResults);
    }

    private <R> List<MetricTagResult> getMetricTags(
            Metrics metrics,
            ExpressionParams<R> expressionParams) {

        return getMetricTagResults(expressionParams, metrics);
    }

    public void addDefaultMetricTags(Bucket4JBootProperties properties, Bucket4JConfiguration filter) {
        if (!properties.getDefaultMetricTags().isEmpty()) {
            var metricTags = filter.getMetrics().getTags();
            var filterMetricTagKeys = metricTags
                    .stream()
                    .map(MetricTag::getKey)
                    .collect(Collectors.toSet());
            properties.getDefaultMetricTags().forEach(defaultTag -> {
                if (!filterMetricTagKeys.contains(defaultTag.getKey())) {
                    metricTags.add(defaultTag);
                }
            });
        }
    }

    private <R> Predicate<R> prepareExecutionPredicates(RateLimit rl, Map<String, ExecutePredicate<R>> executePredicates) {
        return rl.getExecutePredicates()
                .stream()
                .map(p -> createPredicate(p, executePredicates))
                .reduce(Predicate::and)
                .orElseGet(() -> p -> true);
    }

    private <R> Predicate<R> prepareSkipPredicates(RateLimit rl, Map<String, ExecutePredicate<R>> executePredicates) {
        return rl.getSkipPredicates()
                .stream()
                .map(p -> createPredicate(p, executePredicates))
                .reduce(Predicate::and)
                .orElseGet(() -> p -> false);
    }

    protected <R> Predicate<R> createPredicate(ExecutePredicateDefinition pd, Map<String, ExecutePredicate<R>> executePredicates) {
        var predicate = executePredicates.getOrDefault(pd.getName(), null);
        log.debug("create-predicate;name:{};value:{}", pd.getName(), pd.getArgs());
        try {
            @SuppressWarnings("unchecked")
            ExecutePredicate<R> newPredicateInstance = predicate.getClass().getDeclaredConstructor().newInstance();
            return newPredicateInstance.init(pd.getArgs());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new ExecutePredicateInstantiationException(pd.getName(), predicate.getClass());
        }
    }

    public static long getRemainingLimit(Long remaining, RateLimitResult rateLimitResult) {
        if (rateLimitResult != null && (remaining == null || rateLimitResult.getRemainingTokens() < remaining)) {
                remaining = rateLimitResult.getRemainingTokens();
        }
        return remaining;
    }

}
