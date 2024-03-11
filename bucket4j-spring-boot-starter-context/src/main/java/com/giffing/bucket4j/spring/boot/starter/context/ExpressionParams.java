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
@RequiredArgsConstructor
public class ExpressionParams<R> {

    @Getter
    private final R rootObject;

    @Getter
    private final Map<String, Object> params = new HashMap<>();

    public void addParam(String name, Object value) {
        params.put(name, value);
    }

    public ExpressionParams<R> addParams(Map<String, Object> params) {
        this.params.putAll(params);
        return this;
    }

}
