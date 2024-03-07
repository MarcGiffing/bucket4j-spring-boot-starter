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
        @NonNull private List<RateLimit> rateLimits;
        @NonNull private List<MetricHandler> metricHandlers;
        @NonNull private Map<String, ExecutePredicate<R>> executePredicates;
        @NonNull private String cacheName;
        @NonNull private ProxyManagerWrapper proxyWrapper;
        @NonNull private BiFunction<RateLimit, R, String> keyFunction;
        @NonNull private Metrics metrics;
        private long configVersion;
    }

    @Builder
    @Data
    public static class RateLimitConfigresult<R, P> {
        private List<RateLimitCheck<R>> rateLimitChecks;
        private List<PostRateLimitCheck<R, P>> postRateLimitChecks;
    }

    public <R, P> RateLimitConfigresult<R, P> configureRateLimit(RateLimitConfig<R> rateLimitConfig) {


        var metricHandlers = rateLimitConfig.getMetricHandlers();
        var executePredicates = rateLimitConfig.getExecutePredicates();
        var cacheName = rateLimitConfig.getCacheName();
        var metrics = rateLimitConfig.getMetrics();
        var keyFunction = rateLimitConfig.getKeyFunction();
        var proxyWrapper = rateLimitConfig.getProxyWrapper();
        var configVersion = rateLimitConfig.getConfigVersion();

        List<RateLimitCheck<R>> rateLimitChecks = new ArrayList<>();
        List<PostRateLimitCheck<R, P>> postRateLimitChecks = new ArrayList<>();
        rateLimitConfig.getRateLimits().forEach(rl -> {
            log.debug("RL: {}", rl.toString());
            var bucketConfiguration = prepareBucket4jConfigurationBuilder(rl).build();
            var executionPredicate = prepareExecutionPredicates(rl, executePredicates);
            var skipPredicate = prepareSkipPredicates(rl, executePredicates);

            RateLimitCheck<R> rlc = (rootObject, overridableRateLimit) -> {

                var rlToUse = rl.copy();
                rlToUse.consumeNotNullValues(overridableRateLimit);

                var skipRateLimit = performSkipRateLimitCheck(rlToUse, executionPredicate, skipPredicate, rootObject);
                if (!skipRateLimit) {
                    var key = keyFunction.apply(rlToUse, rootObject);
                    var metricBucketListener = createMetricListener(cacheName, metrics, metricHandlers, rootObject);
                    log.debug("try-and-consume;key:{};tokens:{}", key, rlToUse.getNumTokens());
                    return proxyWrapper.tryConsumeAndReturnRemaining(
                            key,
                            rlToUse.getNumTokens(),
                            rlToUse.getPostExecuteCondition() != null,
                            bucketConfiguration,
                            metricBucketListener,
                            configVersion,
                            rlToUse.getTokensInheritanceStrategy()
                    );
                }
                return null;
            };
            rateLimitChecks.add(rlc);


            if (rl.getPostExecuteCondition() != null) {
                log.debug("PRL: {}", rl);
                PostRateLimitCheck<R, P> postRlc = (rootObject, response) -> {
                    var skipRateLimit = performPostSkipRateLimitCheck(rl,
                            executionPredicate, skipPredicate, rootObject, response);
                    if (!skipRateLimit) {
                        var key = keyFunction.apply(rl, rootObject);
                        var metricBucketListener = createMetricListener(cacheName, metrics, metricHandlers, rootObject);
                        log.debug("try-and-consume-post;key:{};tokens:{}", key, rl.getNumTokens());
                        return proxyWrapper.tryConsumeAndReturnRemaining(
                                key,
                                rl.getNumTokens(),
                                false,
                                bucketConfiguration,
                                metricBucketListener,
                                configVersion,
                                rl.getTokensInheritanceStrategy()
                        );
                    }
                    return null;
                };
                postRateLimitChecks.add(postRlc);

            }
        });

        return new RateLimitConfigresult<>(rateLimitChecks, postRateLimitChecks);
    }


    private <R, P> boolean performPostSkipRateLimitCheck(RateLimit rl,
                                                  Predicate<R> executionPredicate,
                                                  Predicate<R> skipPredicate,
                                                  R request,
                                                  P response
    ) {
        var skipRateLimit =  performSkipRateLimitCheck(
                rl, executionPredicate,
                skipPredicate, request);

        if (!skipRateLimit && rl.getPostExecuteCondition() != null) {
            skipRateLimit = !executeResponseCondition(rl).evalute(response);
            log.debug("skip-rate-limit - post-execute-condition: {}", skipRateLimit);
        }

        return skipRateLimit;
    }

    private <R> boolean  performSkipRateLimitCheck(RateLimit rl,
                                                  Predicate<R> executionPredicate,
                                                  Predicate<R> skipPredicate,
                                                  R rootObject) {
        boolean skipRateLimit = false;
        if (rl.getSkipCondition() != null) {
            skipRateLimit = skipCondition(rl).evalute(rootObject);
            log.debug("skip-rate-limit - skip-condition: {}", skipRateLimit);
        }

        if (!skipRateLimit) {
            skipRateLimit = skipPredicate.test(rootObject);
            log.debug("skip-rate-limit - skip-predicates: {}", skipRateLimit);
        }

        if (!skipRateLimit && rl.getExecuteCondition() != null) {
            skipRateLimit = !executeCondition(rl).evalute(rootObject);
            log.debug("skip-rate-limit - execute-condition: {}", skipRateLimit);
        }

        if (!skipRateLimit) {
            skipRateLimit = !executionPredicate.test(rootObject);
            log.debug("skip-rate-limit - execute-predicates: {}", skipRateLimit);
        }
        return skipRateLimit;
    }

    /**
     * Creates the lambda for the execute condition which will be evaluated on each request.
     *
     * @param rateLimit        the {@link RateLimit} configuration which holds the execute condition string
     * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
     */
    private <P> Condition<P> executeResponseCondition(RateLimit rateLimit) {
        return executeExpression(rateLimit.getPostExecuteCondition());
    }

    /**
     * Creates the lambda for the skip condition which will be evaluated on each request
     *
     * @param rateLimit        the {@link RateLimit} configuration which holds the skip condition string
     * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
     */
    private <R> Condition<R> skipCondition(RateLimit rateLimit) {
        if (rateLimit.getSkipCondition() != null) {
            return request -> expressionService.parseBoolean(rateLimit.getSkipCondition(), request);
        }
        return null;
    }


    /**
     * Creates the lambda for the execute condition which will be evaluated on each request.
     *
     * @param rateLimit        the {@link RateLimit} configuration which holds the execute condition string
     * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
     */
    private <R> Condition<R> executeCondition(RateLimit rateLimit) {
        return executeExpression(rateLimit.getExecuteCondition());
    }



    private <R> Condition<R> executeExpression(String condition) {
        if (condition != null) {
            return request -> expressionService.parseBoolean(condition, request);
        }
        return null;
    }

    public <R> List<MetricTagResult> getMetricTagResults(R rootObject, Metrics metrics) {
        return metrics
                .getTags()
                .stream()
                .map(metricMetaTag -> {
                    var value = expressionService.parseString(metricMetaTag.getExpression(), rootObject);
                    return new MetricTagResult(metricMetaTag.getKey(), value, metricMetaTag.getTypes());
                }).toList();
    }

    /**
     * Creates the key filter lambda which is responsible to decide how the rate limit will be performed. The key
     * is the unique identifier like an IP address or a username.
     *
     * @param url              is used to generated a unique cache key
     * @param rateLimit        the {@link RateLimit} configuration which holds the skip condition string
     * @return should not been null. If no filter key type is matching a plain 1 is returned so that all requests uses the same key.
     */
    public <R> KeyFilter<R> getKeyFilter(String url, RateLimit rateLimit) {
        return request -> {
            var value = expressionService.parseString(rateLimit.getCacheKey(), request);
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
                                                      R rootObject) {

        var metricTagResults = getMetricTags(
                metrics,
                rootObject);

        return new MetricBucketListener(
                cacheName,
                metricHandlers,
                metrics.getTypes(),
                metricTagResults);
    }

    private <R> List<MetricTagResult> getMetricTags(
            Metrics metrics,
            R servletRequest) {

        return getMetricTagResults(servletRequest, metrics);
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

}
