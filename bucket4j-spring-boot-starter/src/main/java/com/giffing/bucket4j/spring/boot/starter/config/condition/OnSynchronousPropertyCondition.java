package com.giffing.bucket4j.spring.boot.starter.config.condition;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnSynchronousPropertyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var bucket4jProperties = Binder.get(context.getEnvironment()).bind("bucket4j", Bucket4JBootProperties.class).orElse(null);
        if (bucket4jProperties == null) {
            return ConditionOutcome.noMatch("@ConditionalOnSynchronPropertyCondition Bucket4jBootProperties not configured");
        }
        var methodConfigurationExists = !bucket4jProperties.getMethods().isEmpty();
        var servletConfigurationExist = bucket4jProperties.getFilters().stream().anyMatch(x -> x.getFilterMethod().equals(FilterMethod.SERVLET));
        if (methodConfigurationExists || servletConfigurationExist) {
            return ConditionOutcome.match("@ConditionalOnSynchronPropertyCondition Found filter method and/or servlet configuration");
        } else {
            return ConditionOutcome.noMatch("@ConditionalOnSynchronPropertyCondition No method configuration or filter configuration with FilterMethod.SERVLET configured");
        }
    }
}
