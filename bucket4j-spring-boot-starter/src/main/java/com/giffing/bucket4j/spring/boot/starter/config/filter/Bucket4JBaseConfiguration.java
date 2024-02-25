package com.giffing.bucket4j.spring.boot.starter.config.filter;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheManager;
import com.giffing.bucket4j.spring.boot.starter.config.cache.CacheUpdateListener;
import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway.Bucket4JAutoConfigurationSpringCloudGatewayFilter;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.context.*;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricBucketListener;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.properties.*;
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateInstantiationException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import lombok.extern.slf4j.Slf4j;

/**
 * Holds helper Methods which are reused by the
 * {@link Bucket4JAutoConfigurationServletFilter}
 * {@link Bucket4JAutoConfigurationSpringCloudGatewayFilter}
 * {@link Bucket4JAutoConfigurationWebfluxFilter}
 * configuration classes
 */
@Slf4j
public abstract class Bucket4JBaseConfiguration<R> implements CacheUpdateListener<String, Bucket4JConfiguration> {

    private final CacheManager<String, Bucket4JConfiguration> configCacheManager;

    public Bucket4JBaseConfiguration(CacheManager<String, Bucket4JConfiguration> configCacheManager) {
        this.configCacheManager = configCacheManager;
    }

    public abstract List<MetricHandler> getMetricHandlers();

    public FilterConfiguration<R> buildFilterConfig(
            Bucket4JConfiguration config,
            ProxyManagerWrapper proxyWrapper,
            ExpressionParser expressionParser,
            ConfigurableBeanFactory beanFactory) {

        FilterConfiguration<R> filterConfig = mapFilterConfiguration(config);

        config.getRateLimits().forEach(rl -> {
            log.debug("RL: {}", rl.toString());
            var configurationBuilder = prepareBucket4jConfigurationBuilder(rl);
            var executionPredicate = prepareExecutionPredicates(rl);
            var skipPredicate = prepareSkipPredicates(rl);
            var bucketConfiguration = configurationBuilder.build();
            RateLimitCheck<R> rlc = servletRequest -> {
                var skipRateLimit = performSkipRateLimitCheck(expressionParser, beanFactory, rl, executionPredicate, skipPredicate, servletRequest);
                if (!skipRateLimit) {
                    var key = getKeyFilter(filterConfig.getUrl(), rl, expressionParser, beanFactory).key(servletRequest);
                    var metricBucketListener = createMetricListener(config.getCacheName(), expressionParser, beanFactory, filterConfig, servletRequest);
                    log.debug("try-and-consume;key:{};tokens:{}", key, rl.getNumTokens());
                    final long configVersion = config.getBucket4JVersionNumber();
                    return proxyWrapper.tryConsumeAndReturnRemaining(
                            key,
                            rl.getNumTokens(),
                            bucketConfiguration,
                            metricBucketListener,
                            configVersion,
                            rl.getTokensInheritanceStrategy()
                    );
                }
                return null;
            };
            filterConfig.addRateLimitCheck(rlc);
        });
        return filterConfig;
    }

    private FilterConfiguration<R> mapFilterConfiguration(Bucket4JConfiguration config) {
        FilterConfiguration<R> filterConfig = new FilterConfiguration<>();
        filterConfig.setUrl(config.getUrl().strip());
        filterConfig.setOrder(config.getFilterOrder());
        filterConfig.setStrategy(config.getStrategy());
        filterConfig.setHttpContentType(config.getHttpContentType());
        filterConfig.setHttpResponseBody(config.getHttpResponseBody());
        filterConfig.setHttpStatusCode(config.getHttpStatusCode());
        filterConfig.setHideHttpResponseHeaders(config.getHideHttpResponseHeaders());
        filterConfig.setHttpResponseHeaders(config.getHttpResponseHeaders());
        filterConfig.setMetrics(config.getMetrics());
        return filterConfig;
    }


    private boolean performSkipRateLimitCheck(ExpressionParser expressionParser, ConfigurableBeanFactory beanFactory,
                                              RateLimit rl,
                                              Predicate<R> executionPredicate, Predicate<R> skipPredicate,
                                              R servletRequest) {
        boolean skipRateLimit = false;
        if (rl.getSkipCondition() != null) {
            skipRateLimit = skipCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
            log.debug("skip-rate-limit - skip-condition: {}", skipRateLimit);
        }

        if (!skipRateLimit) {
            skipRateLimit = skipPredicate.test(servletRequest);
            log.debug("skip-rate-limit - skip-predicates: {}", skipRateLimit);
        }

        if (!skipRateLimit && rl.getExecuteCondition() != null) {
            skipRateLimit = !executeCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
            log.debug("skip-rate-limit - execute-condition: {}", skipRateLimit);
        }

        if (!skipRateLimit) {
            skipRateLimit = !executionPredicate.test(servletRequest);
            log.debug("skip-rate-limit - execute-predicates: {}", skipRateLimit);
        }
        return skipRateLimit;
    }

    protected abstract ExecutePredicate<R> getExecutePredicateByName(String name);

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

    private MetricBucketListener createMetricListener(String cacheName,
                                                      ExpressionParser expressionParser,
                                                      ConfigurableBeanFactory beanFactory,
                                                      FilterConfiguration<R> filterConfig,
                                                      R servletRequest) {

        var metricTagResults = getMetricTags(
                expressionParser,
                beanFactory,
                filterConfig,
                servletRequest);

        return new MetricBucketListener(
                cacheName,
                getMetricHandlers(),
                filterConfig.getMetrics().getTypes(),
                metricTagResults);
    }

    private List<MetricTagResult> getMetricTags(
            ExpressionParser expressionParser,
            ConfigurableBeanFactory beanFactory,
            FilterConfiguration<R> filterConfig,
            R servletRequest) {

        return filterConfig
                .getMetrics()
                .getTags()
                .stream()
                .map(metricMetaTag -> {
                    var context = new StandardEvaluationContext();
                    context.setBeanResolver(new BeanFactoryResolver(beanFactory));
                    //TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
                    var expr = expressionParser.parseExpression(metricMetaTag.getExpression());
                    var value = expr.getValue(context, servletRequest, String.class);

                    return new MetricTagResult(metricMetaTag.getKey(), value, metricMetaTag.getTypes());
                }).toList();
    }

    /**
     * Creates the key filter lambda which is responsible to decide how the rate limit will be performed. The key
     * is the unique identifier like an IP address or a username.
     *
     * @param url              is used to generated a unique cache key
     * @param rateLimit        the {@link RateLimit} configuration which holds the skip condition string
     * @param expressionParser is used to evaluate the expression if the filter key type is EXPRESSION.
     * @param beanFactory      used to get full access to all java beans in the SpEl
     * @return should not been null. If no filter key type is matching a plain 1 is returned so that all requests uses the same key.
     */
    public KeyFilter<R> getKeyFilter(String url, RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
        var cacheKeyexpression = rateLimit.getCacheKey();
        var context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return request -> {
            //TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
            Expression expr = expressionParser.parseExpression(cacheKeyexpression);
            final String value = expr.getValue(context, request, String.class);
            return url + "-" + value;
        };
    }

    /**
     * Creates the lambda for the skip condition which will be evaluated on each request
     *
     * @param rateLimit        the {@link RateLimit} configuration which holds the skip condition string
     * @param expressionParser is used to evaluate the skip expression
     * @param beanFactory      used to get full access to all java beans in the SpEl
     * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
     */
    public Condition<R> skipCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
        var context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));

        if (rateLimit.getSkipCondition() != null) {
            return request -> {
                Expression expr = expressionParser.parseExpression(rateLimit.getSkipCondition());
                return expr.getValue(context, request, Boolean.class);
            };
        }
        return null;
    }

    /**
     * Creates the lambda for the execute condition which will be evaluated on each request.
     *
     * @param rateLimit        the {@link RateLimit} configuration which holds the execute condition string
     * @param expressionParser is used to evaluate the execution expression
     * @param beanFactory      used to get full access to all java beans in the SpEl
     * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
     */
    public Condition<R> executeCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
        var context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));

        if (rateLimit.getExecuteCondition() != null) {
            return request -> {
                Expression expr = expressionParser.parseExpression(rateLimit.getExecuteCondition());
                return expr.getValue(context, request, Boolean.class);
            };
        }
        return null;
    }

    protected void addDefaultMetricTags(Bucket4JBootProperties properties, Bucket4JConfiguration filter) {
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

    private Predicate<R> prepareExecutionPredicates(RateLimit rl) {
        return rl.getExecutePredicates()
                .stream()
                .map(this::createPredicate)
                .reduce(Predicate::and)
                .orElseGet(() -> p -> true);
    }

    private Predicate<R> prepareSkipPredicates(RateLimit rl) {
        return rl.getSkipPredicates()
                .stream()
                .map(this::createPredicate)
                .reduce(Predicate::and)
                .orElseGet(() -> p -> false);
    }

    protected Predicate<R> createPredicate(ExecutePredicateDefinition pd) {
        var predicate = getExecutePredicateByName(pd.getName());
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

    /**
     * Try to load a filter configuration from the cache with the same id as the provided filter.
     * <p>
     * If caching is disabled or no matching filter is found, the provided filter will be added to the cache and returned to the caller.
     * If the provided filter has a higher version than the cached filter, the cache will be overridden and the provided filter will be returned.
     * If the cached filter has a higher or equal version, the cached filter will be returned.
     *
     * @param filter the Bucket4JConfiguration to find or update in the cache. The id of this filter should not be null.
     * @return returns the Bucket4JConfiguration with the highest version, either the cached or provided filter.
     */
    protected Bucket4JConfiguration getOrUpdateConfigurationFromCache(Bucket4JConfiguration filter) {
        //if caching is disabled or if the filter does not have an id, return the provided filter
        if (this.configCacheManager == null || filter.getId() == null) return filter;

        Bucket4JConfiguration cachedFilter = this.configCacheManager.getValue(filter.getId());
        if (cachedFilter != null && cachedFilter.getBucket4JVersionNumber() >= filter.getBucket4JVersionNumber()) {
            return cachedFilter;
        } else {
            this.configCacheManager.setValue(filter.getId(), filter);
        }
        return filter;
    }

}
