package com.giffing.bucket4j.spring.boot.starter.config;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import com.giffing.bucket4j.spring.boot.starter.config.cache.ProxyManagerWrapper;
import com.giffing.bucket4j.spring.boot.starter.config.gateway.Bucket4JAutoConfigurationSpringCloudGatewayFilter;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.config.webflux.Bucket4JAutoConfigurationWebfluxFilter;
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
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateBeanNotFoundException;
import com.giffing.bucket4j.spring.boot.starter.exception.ExecutePredicateInstantiationException;
import com.giffing.bucket4j.spring.boot.starter.exception.FilterURLInvalidException;
import com.giffing.bucket4j.spring.boot.starter.exception.MissingKeyFilterExpressionException;
import com.giffing.bucket4j.spring.boot.starter.exception.MissingMetricTagExpressionException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.Refill;
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
		
		throwExceptionOnInvalidFilterUrl(filterConfig);
		
		config.getRateLimits().forEach(rl -> {
			log.debug("RL: {}",rl.toString());
			final ConfigurationBuilder configurationBuilder = prepareBucket4jConfigurationBuilder(rl);
			Predicate<R> executionPredicate = prepareExecutionPredicates(rl);
			RateLimitCheck<R> rlc = (servletRequest) -> {
				
		        boolean skipRateLimit = false;
		        if (rl.getSkipCondition() != null) {
		        	skipRateLimit = skipCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
		        	log.debug("skip-rate-limit - skip-condition: {}", skipRateLimit);
		        } 
		        
		        if(rl.getExecuteCondition() != null && !skipRateLimit) {
		        	skipRateLimit = !executeCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
		        	log.debug("skip-rate-limit - execute-condition: {}", skipRateLimit);
		        }
		        
		        if(!skipRateLimit) {
		        	skipRateLimit = !executionPredicate.test(servletRequest);
		        	log.debug("skip-rate-limit - execute-predicates: {}", skipRateLimit);
		        }
		        
		        if(!skipRateLimit) {
		        	String key = getKeyFilter(filterConfig.getUrl(), rl, expressionParser, beanFactory).key(servletRequest);
		        	BucketConfiguration bucketConfiguration = configurationBuilder.build();
		        	
		        	List<MetricTagResult> metricTagResults = getMetricTags(
		        			expressionParser, 
		        			beanFactory, 
		        			filterConfig,
							servletRequest);

		        	MetricBucketListener metricBucketListener = new MetricBucketListener(
							config.getCacheName(),
							getMetricHandlers(), 
							filterConfig.getMetrics().getTypes(),
							metricTagResults);
		        	
		        	log.debug("try-and-consume;key:{};tokens:{}", key, rl.getNumTokens());
		        	return proxyWrapper.tryConsumeAndReturnRemaining(key, rl.getNumTokens(), bucketConfiguration, metricBucketListener);
		        	
		        }
				return null;
				
			};
			filterConfig.getRateLimitChecks().add(rlc);
			
		});
		
		return filterConfig;
	}
	
	protected void validate(Bucket4JConfiguration config) {
		validateExecutePredicates(config);
	}

	protected abstract ExecutePredicate<R> getExecutePredicateByName(String name);
	
	private void throwExceptionOnInvalidFilterUrl(FilterConfiguration<R> filterConfig) {
		try {
			Pattern.compile(filterConfig.getUrl());
			if(filterConfig.getUrl().equals("/*")) {
				throw new PatternSyntaxException(filterConfig.getUrl(), "/*", 0);
			}
		} catch( PatternSyntaxException exception) {
			throw new FilterURLInvalidException(filterConfig.getUrl(), exception.getDescription());
		}
	}

	private ConfigurationBuilder prepareBucket4jConfigurationBuilder(RateLimit rl) {
		ConfigurationBuilder configBuilder = BucketConfiguration.builder();
		for (BandWidth bandWidth : rl.getBandwidths()) {
			Bandwidth bucket4jBandWidth = null;
			long capacity = bandWidth.getCapacity();
			long refillCapacity = bandWidth.getRefillCapacity() != null ? bandWidth.getRefillCapacity() : bandWidth.getCapacity();
			Duration refillPeriod = Duration.of(bandWidth.getTime(), bandWidth.getUnit());
			switch(bandWidth.getRefillSpeed()) {
				case GREEDY:
					bucket4jBandWidth = Bandwidth.classic(capacity, Refill.greedy(refillCapacity, refillPeriod));
					break;
				case INTERVAL:
					bucket4jBandWidth = Bandwidth.classic(capacity, Refill.intervally(refillCapacity, refillPeriod));
					break;
				default:
					throw new IllegalStateException("Unsupported Refill type: " + bandWidth.getRefillSpeed());
			}
			if(bandWidth.getInitialCapacity() != null) {
				bucket4jBandWidth = bucket4jBandWidth.withInitialTokens(bandWidth.getInitialCapacity());
			}
			configBuilder = configBuilder.addLimit(bucket4jBandWidth);
		}
		return configBuilder;
	}

	private List<MetricTagResult> getMetricTags(
			ExpressionParser expressionParser, 
			ConfigurableBeanFactory beanFactory,
			FilterConfiguration<R> filterConfig, 
			R servletRequest) {
		
		List<MetricTagResult> metricTagResults = filterConfig
			.getMetrics().getTags()
			.stream()
			.map( metricMetaTag -> {
				String expression = metricMetaTag.getExpression();
				if(!StringUtils.hasText(expression)) {
					throw new MissingMetricTagExpressionException(metricMetaTag.getKey());
				}
				StandardEvaluationContext context = new StandardEvaluationContext();
				context.setBeanResolver(new BeanFactoryResolver(beanFactory));
				//TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
				Expression expr = expressionParser.parseExpression(expression); 
				final String value = expr.getValue(context, servletRequest, String.class);
				
				return new MetricTagResult(metricMetaTag.getKey(), value, metricMetaTag.getTypes());
		}).collect(toList());
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
		
		String cacheKeyexpression = rateLimit.getCacheKey();
		if(!StringUtils.hasText(cacheKeyexpression)) {
			throw new MissingKeyFilterExpressionException();
		}
		StandardEvaluationContext context = new StandardEvaluationContext();
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
		StandardEvaluationContext context = new StandardEvaluationContext();
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
		StandardEvaluationContext context = new StandardEvaluationContext();
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
			List<MetricTag> metricTags = filter.getMetrics().getTags();
			Set<String> filterMetricTagKeys = metricTags.stream().map(MetricTag::getKey).collect(Collectors.toSet());
			properties.getDefaultMetricTags().forEach(defaultTag -> {
				if(!filterMetricTagKeys.contains(defaultTag.getKey())) {
					metricTags.add(defaultTag);
				}
			});
		}
	}
	
	private void validateExecutePredicates(Bucket4JConfiguration config) {
		var allExecutePredicateNames = config
				.getRateLimits()
				.stream()
				.map(r -> r.getExecutePredicates())
				.flatMap(List::stream)
				.map(x -> x.getName())
				.distinct().collect(Collectors.toSet());
			allExecutePredicateNames.forEach(predicateName -> {
				if(getExecutePredicateByName(predicateName) == null) {
					throw new ExecutePredicateBeanNotFoundException(predicateName);
				}
			});
	}
	
	protected Predicate<R> createExecutionPredicate(ExecutePredicateDefinition pd) {
		ExecutePredicate<R> predicate = getExecutePredicateByName(pd.getName());
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
	
	private Predicate<R> prepareExecutionPredicates(RateLimit rl) {
		return rl.getExecutePredicates()
        		.stream()
        		.map(p -> createExecutionPredicate(p))
        		.reduce( (p1, p2) -> p1.and(p2))
        		.orElseGet(() -> (R) -> true);
	}
	
}
