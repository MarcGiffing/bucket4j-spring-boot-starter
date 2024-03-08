package com.giffing.bucket4j.spring.boot.starter.service;

import com.giffing.bucket4j.spring.boot.starter.context.ExpressionParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * The expression service wraps Springs {@link ExpressionParser} to execute SpEl expressions.
 */
@RequiredArgsConstructor
@Slf4j
public class ExpressionService {

    private final ExpressionParser expressionParser;

    private final ConfigurableBeanFactory beanFactory;

    public <R> String parseString(String expression, ExpressionParams<R> params) {
        var context = getContext(params.getParams());
        var expr = expressionParser.parseExpression(expression);
        String result = expr.getValue(context, params.getRootObject(), String.class);
        log.info("parse-string-expression;result:{};expression:{};root:{};params:{}", result, expression, params.getRootObject(), params.getParams());
        return result;
    }

    public <R> Boolean parseBoolean(String expression, ExpressionParams<R> params) {
        var context = getContext(params.getParams());
        var expr = expressionParser.parseExpression(expression);
        boolean result = Boolean.TRUE.equals(expr.getValue(context, params.getRootObject(), Boolean.class));
        log.info("parse-boolean-expression;result:{};expression:{};root:{};params:{}", result, expression, params.getRootObject(), params.getParams());
        return result;
    }

    private StandardEvaluationContext getContext(Map<String, Object> params) {
        var context = new StandardEvaluationContext();
        params.forEach(context::setVariable);
        context.setBeanResolver(new BeanFactoryResolver(beanFactory));
        return context;
    }
}
