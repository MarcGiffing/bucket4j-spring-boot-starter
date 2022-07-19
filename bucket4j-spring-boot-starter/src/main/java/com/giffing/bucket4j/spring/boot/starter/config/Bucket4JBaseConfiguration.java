package com.giffing.bucket4j.spring.boot.starter.config;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

import com.giffing.bucket4j.spring.boot.starter.config.gateway.Bucket4JAutoConfigurationSpringCloudGatewayFilter;
import com.giffing.bucket4j.spring.boot.starter.config.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.config.webflux.Bucket4JAutoConfigurationWebfluxFilter;
import com.giffing.bucket4j.spring.boot.starter.context.Condition;
import com.giffing.bucket4j.spring.boot.starter.context.ConsumptionProbeHolder;
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
import com.giffing.bucket4j.spring.boot.starter.exception.FilterURLInvalidException;
import com.giffing.bucket4j.spring.boot.starter.exception.MissingKeyFilterExpressionException;
import com.giffing.bucket4j.spring.boot.starter.exception.MissingMetricTagExpressionException;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConfigurationBuilder;
import io.github.bucket4j.Refill;
import io.github.bucket4j.distributed.AsyncBucketProxy;
import io.github.bucket4j.distributed.proxy.ProxyManager;

/**
 * Holds helper Methods which are reused by the 
 * {@link Bucket4JAutoConfigurationServletFilter}
 * {@link Bucket4JAutoConfigurationSpringCloudGatewayFilter} 
 * {@link Bucket4JAutoConfigurationWebfluxFilter} 
 * configuration classes
 */
public abstract class Bucket4JBaseConfiguration<R> {
	
	public abstract List<MetricHandler> getMetricHandlers();
	
	public FilterConfiguration<R> buildFilterConfig(
			Bucket4JConfiguration config, 
			ProxyManager<String> buckets,
			ExpressionParser expressionParser, 
			ConfigurableBeanFactory  beanFactory) {
		
		FilterConfiguration<R> filterConfig = new FilterConfiguration<>();
		filterConfig.setUrl(StringUtils.trimWhitespace(config.getUrl()));
		filterConfig.setOrder(config.getFilterOrder());
		filterConfig.setStrategy(config.getStrategy());
		filterConfig.setHttpContentType(config.getHttpContentType());
		filterConfig.setHttpResponseBody(config.getHttpResponseBody());
		filterConfig.setHideHttpResponseHeaders(config.getHideHttpResponseHeaders());
		filterConfig.setHttpResponseHeaders(config.getHttpResponseHeaders());
		filterConfig.setMetrics(config.getMetrics());
		
		throwExceptionOnInvalidFilterUrl(filterConfig);
		
		config.getRateLimits().forEach(rl -> {
			
			final ConfigurationBuilder configurationBuilder = prepareBucket4jConfigurationBuilder(rl);
			
			RateLimitCheck<R> rlc = (servletRequest, async) -> {
				
		        boolean skipRateLimit = false;
		        if (rl.getSkipCondition() != null) {
		        	skipRateLimit = skipCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
		        } 
		        
		        if(rl.getExecuteCondition() != null && !skipRateLimit) {
		        	skipRateLimit = !executeCondition(rl, expressionParser, beanFactory).evalute(servletRequest);
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

					if (async) {
						AsyncBucketProxy bucket = buckets.asAsync().builder().build(key, bucketConfiguration).toListenable(metricBucketListener);
						return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(rl.getCost()));
					} else {
						Bucket bucket = buckets.builder().build(key, bucketConfiguration).toListenable(metricBucketListener);
						return new ConsumptionProbeHolder(bucket.tryConsumeAndReturnRemaining(rl.getCost()));
					}
		        	
		        }
				return null;
				
			};
			filterConfig.getRateLimitChecks().add(rlc);
			
		});
		
		return filterConfig;
	}

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
				Bandwidth bucket4jBandWidth = Bandwidth.simple(bandWidth.getCapacity(), Duration.of(bandWidth.getTime(), bandWidth.getUnit()));
				if(bandWidth.getFixedRefillInterval() > 0) {
					bucket4jBandWidth = Bandwidth.classic(bandWidth.getCapacity(), Refill.intervally(bandWidth.getCapacity(), Duration.of(bandWidth.getFixedRefillInterval(), bandWidth.getFixedRefillIntervalUnit())));
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
		
		String expression = rateLimit.getExpression();
		if(!StringUtils.hasText(expression)) {
			throw new MissingKeyFilterExpressionException();
		}
		StandardEvaluationContext context = new StandardEvaluationContext();
		context.setBeanResolver(new BeanFactoryResolver(beanFactory));
		return  request -> {
			//TODO performance problem - how can the request object reused in the expression without setting it as a rootObject
			Expression expr = expressionParser.parseExpression(rateLimit.getExpression()); 
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
	 * @return the lamdba condition which will be evaluated lazy - null if there is no condition available.
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
	 * @return the lamdba condition which will be evaluated lazy - null if there is no condition available.
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
	
}
