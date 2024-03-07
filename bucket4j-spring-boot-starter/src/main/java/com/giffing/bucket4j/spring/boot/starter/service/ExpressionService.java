package com.giffing.bucket4j.spring.boot.starter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

@RequiredArgsConstructor
public class ExpressionService {

    private final ExpressionParser expressionParser;

    private final ConfigurableBeanFactory beanFactory;

    public String parseString(String expression, Object rootObject) {
        var expr = expressionParser.parseExpression(expression);
        return expr.getValue(getContext(), rootObject, String.class);
    }

    public Boolean parseBoolean(String expression, Object request) {
        var expr = expressionParser.parseExpression(expression);
        return Boolean.TRUE.equals(expr.getValue(getContext(), request, Boolean.class));
    }

    private StandardEvaluationContext getContext() {
        var context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return context;
    }
}
