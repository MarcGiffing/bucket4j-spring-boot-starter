package com.giffing.bucket4j.spring.boot.starter.context;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.Expression;

import java.util.HashMap;
import java.util.Map;

/**
 * Parameter information for the evaluation of a Spring {@link Expression}
 *
 * @param <R> the type of the root object which us used for the SpEl expression.
 */
@Getter
@RequiredArgsConstructor
public class ExpressionParams<R> {

    private final R rootObject;

    private final Map<String, Object> params = new HashMap<>();

    public ExpressionParams<R> addParam(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public ExpressionParams<R> addParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

}
