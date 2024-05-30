package com.giffing.bucket4j.spring.boot.starter.config.condition;

import com.giffing.bucket4j.spring.boot.starter.context.FilterMethod;
import com.giffing.bucket4j.spring.boot.starter.context.properties.Bucket4JBootProperties;
import org.springframework.boot.autoconfigure.condition.ConditionOutcome;
import org.springframework.boot.autoconfigure.condition.SpringBootCondition;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.List;

public class OnAsynchronousPropertyCondition extends SpringBootCondition {
    @Override
    public ConditionOutcome getMatchOutcome(ConditionContext context, AnnotatedTypeMetadata metadata) {
        var bucket4jProperties = Binder.get(context.getEnvironment()).bind("bucket4j", Bucket4JBootProperties.class).orElse(null);
        if (bucket4jProperties != null) {
            var reactiveFilterConfigurationExists = bucket4jProperties.getFilters()
                    .stream()
                    .anyMatch(x -> List.of(FilterMethod.WEBFLUX, FilterMethod.GATEWAY).contains(x.getFilterMethod()));
            if (reactiveFilterConfigurationExists) {
                return ConditionOutcome.match("@ConditionalOnAsynchronousPropertyCondition Found reactive filter");
            } else {
                return ConditionOutcome.noMatch("@ConditionalOnAsynchronousPropertyCondition No filter configuration with FilterMethod.WEBFLUX org FilterMethod.GATEWAY configured");
            }
        } else {
            return ConditionOutcome.noMatch("@ConditionalOnAsynchronousPropertyCondition Bucket4jBootProperties not configured");
        }
    }
}
