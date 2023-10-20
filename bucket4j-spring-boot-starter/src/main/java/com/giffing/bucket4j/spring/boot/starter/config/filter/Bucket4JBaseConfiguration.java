package com.giffing.bucket4j.spring.boot.starter.config.filter;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.giffing.bucket4j.spring.boot.starter.exception.*;
import io.github.bucket4j.*;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.gateway.Bucket4JAutoConfigurationSpringCloudGatewayFilter;
import com.giffing.bucket4j.spring.boot.starter.config.filter.reactive.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.context.Condition;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.KeyFilter;
import com.giffing.bucket4j.spring.boot.starter.context.RateLimitCheck;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricBucketListener;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricTagResult;
import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.FilterConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.MetricTag;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;

import lombok.extern.slf4j.Slf4j;

/**
 * Holds helper Methods which are reused by the 
 * {@link Bucket4JAutoConfigurationServletFilter}
 * {@link Bucket4JAutoConfigurationSpringCloudGatewayFilter} 
 * {@link Bucket4JAutoConfigurationWebfluxFilter} 
 * configuration classes
 */
@Slf4j
public abstract class Bucket4JBaseConfiguration<R> {
	
	public abstract List<MetricHandler> getMetricHandlers();
	
	public FilterConfiguration<R> buildFilterConfig(
			Bucket4JConfiguration config, 
			ProxyManagerWrapper proxyWrapper,
			ExpressionParser expressionParser, 
			ConfigurableBeanFactory  beanFactory) {
		
		validate(config);
		
		FilterConfiguration<R> filterConfig = mapFilterConfiguration(config);
		
		config.getRateLimits().forEach(rl -> {
			log.debug("RL: {}",rl.toString());
			var configurationBuilder = prepareBucket4jConfigurationBuilder(rl);
			var executionPredicate = prepareExecutionPredicates(rl);
			var skipPredicate = prepareSkipPredicates(rl);
			var bucketConfiguration = configurationBuilder.build();

			RateLimitCheck<R> rlc = servletRequest -> {
		        var skipRateLimit = performSkipRateLimitCheck(expressionParser, beanFactory, rl, executionPredicate, skipPredicate, servletRequest);
		        if(!skipRateLimit) {
		        	var key = getKeyFilter(filterConfig.getUrl(), rl, expressionParser, beanFactory).key(servletRequest);
		        	var metricBucketListener = createMetricListener(config.getCacheName(), expressionParser, beanFactory, filterConfig, servletRequest);
		        	log.debug("try-and-consume;key:{};tokens:{}", key, rl.getNumTokens());
		        	return proxyWrapper.tryConsumeAndReturnRemaining(key, rl.getNumTokens(), bucketConfiguration, metricBucketListener);
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
	
	protected void validate(Bucket4JConfiguration config) {
		throwExceptionOnInvalidFilterUrl(config);
		validateRateLimit(config);
		validatePredicates(config);
		validateMetricTags(config);
	}
	
	private void throwExceptionOnInvalidFilterUrl(Bucket4JConfiguration filterConfig) {
		try {
			Pattern.compile(filterConfig.getUrl());
			if(filterConfig.getUrl().equals("/*")) {
				throw new PatternSyntaxException(filterConfig.getUrl(), "/*", 0);
			}
		} catch( PatternSyntaxException exception) {
			throw new FilterURLInvalidException(filterConfig.getUrl(), exception.getDescription());
		}
	}
	
	private void validateRateLimit(Bucket4JConfiguration config) {
		config
			.getRateLimits()
			.forEach(rl -> {
				var cacheKeyexpression = rl.getCacheKey();
				if (!StringUtils.hasText(cacheKeyexpression)) {
					throw new MissingKeyFilterExpressionException();
				}
				// When there is more than 1 bandwidth, they either need to have an ID, or TokensInheritanceStrategy.RESET has to
				// be applied when replacing a Bucket4j bucket configuration. In either case the id's cannot be duplicate.
				if(rl.getBandwidths().size() > 1){
					validateBandwidths(config.getId(), rl.getBandwidths(), rl.getTokensInheritanceStrategy() == TokensInheritanceStrategy.RESET);
				}
			});
	}

	private void validateBandwidths(String filterId, List<BandWidth> bandwidths, boolean allowNullIds){
		Set<String> idSet = new HashSet<>();
		for (BandWidth bandWidth : bandwidths) {
			String id = bandWidth.getId();
			if(id == null && allowNullIds) continue;
			if (!idSet.add(bandWidth.getId())) {
				throw new DuplicateBandwidthIDException(filterId, bandWidth.getId());
			}
		}
	}

	private void validatePredicates(Bucket4JConfiguration config) {
		var executePredicates = config
				.getRateLimits()
				.stream()
				.map(r -> r.getExecutePredicates());
		var skipPredicates = config
				.getRateLimits()
				.stream()
				.map(r -> r.getSkipPredicates());
		
		var allExecutePredicateNames = Stream.concat(executePredicates, skipPredicates)
				.flatMap(List::stream)
				.map(x -> x.getName())
				.distinct().collect(Collectors.toSet());
			allExecutePredicateNames.forEach(predicateName -> {
				if(getExecutePredicateByName(predicateName) == null) {
					throw new ExecutePredicateBeanNotFoundException(predicateName);
				}
			});
	}
	
	private void validateMetricTags(Bucket4JConfiguration config) {
		config
			.getMetrics()
			.getTags()
			.stream()
			.forEach(metricMetaTag -> {
			String expression = metricMetaTag.getExpression();
			if (!StringUtils.hasText(expression)) {
				throw new MissingMetricTagExpressionException(metricMetaTag.getKey());
			}
		});
		
	}

	private ConfigurationBuilder prepareBucket4jConfigurationBuilder(RateLimit rl) {
		var configBuilder = BucketConfiguration.builder();
		for (BandWidth bandWidth : rl.getBandwidths()) {
			long capacity = bandWidth.getCapacity();
			long refillCapacity = bandWidth.getRefillCapacity() != null ? bandWidth.getRefillCapacity() : bandWidth.getCapacity();
			var refillPeriod = Duration.of(bandWidth.getTime(), bandWidth.getUnit());
			var bucket4jBandWidth = switch(bandWidth.getRefillSpeed()) {
				case GREEDY -> Bandwidth.classic(capacity, Refill.greedy(refillCapacity, refillPeriod)).withId(bandWidth.getId());
				case INTERVAL -> Bandwidth.classic(capacity, Refill.intervally(refillCapacity, refillPeriod)).withId(bandWidth.getId());
				default -> throw new IllegalStateException("Unsupported Refill type: " + bandWidth.getRefillSpeed());
			};
			if(bandWidth.getInitialCapacity() != null) {
				bucket4jBandWidth = bucket4jBandWidth.withInitialTokens(bandWidth.getInitialCapacity());
			}
			configBuilder = configBuilder.addLimit(bucket4jBandWidth);
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
		
		var metricTagResults = filterConfig
			.getMetrics()
			.getTags()
			.stream()
			.map( metricMetaTag -> {
				var context = new StandardEvaluationContext();
				context.setBeanResolver(new BeanFactoryResolver(beanFactory));
				//TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
				var expr = expressionParser.parseExpression(metricMetaTag.getExpression()); 
				var value = expr.getValue(context, servletRequest, String.class);
				
				return new MetricTagResult(metricMetaTag.getKey(), value, metricMetaTag.getTypes());
		}).toList();
		if(metricTagResults == null) {
			metricTagResults = new ArrayList<>();
		}
		return metricTagResults;
	}

	/**
	 * Creates the key filter lambda which is responsible to decide how the rate limit will be performed. The key
	 * is the unique identifier like an IP address or a username.
	 * 
	 * @param url is used to generated a unique cache key
	 * @param rateLimit the {@link RateLimit} configuration which holds the skip condition string
	 * @param expressionParser is used to evaluate the expression if the filter key type is EXPRESSION.
	 * @param beanFactory used to get full access to all java beans in the SpEl
	 * @return should not been null. If no filter key type is matching a plain 1 is returned so that all requests uses the same key.
	 */
	public KeyFilter<R> getKeyFilter(String url, RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		var cacheKeyexpression = rateLimit.getCacheKey();
		var context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		return  request -> {
			//TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
			Expression expr = expressionParser.parseExpression(cacheKeyexpression); 
			final String value = expr.getValue(context, request, String.class);
			return url + "-" + value;
		};
	}
	
	/**
	 * Creates the lambda for the skip condition which will be evaluated on each request
	 * 
	 * @param rateLimit the {@link RateLimit} configuration which holds the skip condition string
	 * @param expressionParser is used to evaluate the skip expression
	 * @param beanFactory used to get full access to all java beans in the SpEl
	 * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
	 */
	public Condition<R> skipCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		var context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		
		if(rateLimit.getSkipCondition() != null) {
			return  request -> {
				Expression expr = expressionParser.parseExpression(rateLimit.getSkipCondition()); 
				return expr.getValue(context, request, Boolean.class);
			};
		}
		return null;
	}
	
	/**
	 * Creates the lambda for the execute condition which will be evaluated on each request.
	 * 
	 * @param rateLimit the {@link RateLimit} configuration which holds the execute condition string
	 * @param expressionParser is used to evaluate the execution expression
	 * @param beanFactory used to get full access to all java beans in the SpEl
	 * @return the lambda condition which will be evaluated lazy - null if there is no condition available.
	 */
	public Condition<R> executeCondition(RateLimit rateLimit, ExpressionParser expressionParser, BeanFactory beanFactory) {
		var context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		
		if(rateLimit.getExecuteCondition() != null) {
			return request -> {
				Expression expr = expressionParser.parseExpression(rateLimit.getExecuteCondition()); 
				return expr.getValue(context, request, Boolean.class);
			};
		}
		return null;
	}
	
	protected void addDefaultMetricTags(Bucket4JBootProperties properties, Bucket4JConfiguration filter) {
		if(!properties.getDefaultMetricTags().isEmpty()) {
			var metricTags = filter.getMetrics().getTags();
			var filterMetricTagKeys = metricTags
					.stream()
					.map(MetricTag::getKey)
					.collect(Collectors.toSet());
			properties.getDefaultMetricTags().forEach(defaultTag -> {
				if(!filterMetricTagKeys.contains(defaultTag.getKey())) {
					metricTags.add(defaultTag);
				}
			});
		}
	}
	
	private Predicate<R> prepareExecutionPredicates(RateLimit rl) {
		return rl.getExecutePredicates()
        		.stream()
        		.map(this::createPredicate)
        		.reduce( Predicate::and)
        		.orElseGet(() -> p -> true);
	}
	
	private Predicate<R> prepareSkipPredicates(RateLimit rl) {
		return rl.getSkipPredicates()
        		.stream()
        		.map(this::createPredicate)
        		.reduce( Predicate::and)
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
	
}
