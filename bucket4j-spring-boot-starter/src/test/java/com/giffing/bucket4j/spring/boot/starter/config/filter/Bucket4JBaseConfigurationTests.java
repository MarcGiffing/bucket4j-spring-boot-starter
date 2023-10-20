package com.giffing.bucket4j.spring.boot.starter.config.filter;

import com.giffing.bucket4j.spring.boot.starter.config.cache.SyncCacheResolver;
import com.giffing.bucket4j.spring.boot.starter.config.filter.servlet.Bucket4JAutoConfigurationServletFilter;
import com.giffing.bucket4j.spring.boot.starter.context.Bucket4jConfigurationHolder;
import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicate;
import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.RefillSpeed;
import com.giffing.bucket4j.spring.boot.starter.context.metrics.MetricHandler;
import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JConfiguration;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import io.github.bucket4j.TokensInheritanceStrategy;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.ExpressionParser;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;

public class Bucket4JBaseConfigurationTests {

	Bucket4JConfiguration filterConfig;

	Bucket4JAutoConfigurationServletFilter filter;
	ConfigurableServletWebServerFactory webServerFactory;

	@BeforeEach
	public void setup() {
		filterConfig = new Bucket4JConfiguration();
		filterConfig.setUrl("dummyUrl");
		filterConfig.setFilterMethod(FilterMethod.SERVLET);

		RateLimit limit = new RateLimit();
		limit.setTokensInheritanceStrategy(TokensInheritanceStrategy.ADDITIVE);

		List<BandWidth> bandWidths = new ArrayList<>();
		BandWidth bandWidth = new BandWidth();
//		bandWidth.setId("");
		bandWidth.setCapacity(10);
		bandWidth.setTime(10);
		bandWidth.setUnit(ChronoUnit.DAYS);
		bandWidth.setRefillCapacity(100L);
		bandWidth.setRefillSpeed(RefillSpeed.INTERVAL);
		bandWidths.add(bandWidth);

		BandWidth bandWidth2 = new BandWidth();
//		bandWidth.setId("");
		bandWidth.setCapacity(10);
		bandWidth.setTime(10);
		bandWidth.setUnit(ChronoUnit.DAYS);
		bandWidth.setRefillCapacity(100L);
		bandWidth.setRefillSpeed(RefillSpeed.INTERVAL);
		bandWidths.add(bandWidth2);

		limit.setBandwidths(bandWidths);
		filterConfig.setRateLimits(Collections.singletonList(limit));

		Bucket4JBootProperties properties = new Bucket4JBootProperties();
		properties.setFilters(Collections.singletonList(filterConfig));

		ConfigurableBeanFactory beanFactory = mock();
		GenericApplicationContext context = mock();
		SyncCacheResolver cacheResolver = mock();
		List<MetricHandler> metricHandlers = mock();
		List<ExecutePredicate<HttpServletRequest>> executePredicates = mock();
		Bucket4jConfigurationHolder servletConfigurationHolder = mock();
		ExpressionParser servletFilterExpressionParser = mock();

		filter = new Bucket4JAutoConfigurationServletFilter(properties, beanFactory, context, cacheResolver,
				metricHandlers, executePredicates, servletConfigurationHolder, servletFilterExpressionParser);
		webServerFactory = mock(ConfigurableServletWebServerFactory.class);
	}

	@Test
	public void setFilterIdTest() {
		filter.customize(webServerFactory);
	}
}
