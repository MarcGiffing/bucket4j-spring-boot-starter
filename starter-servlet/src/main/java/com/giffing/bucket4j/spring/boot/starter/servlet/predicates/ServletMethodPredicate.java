package com.giffing.bucket4j.spring.boot.starter.servlet.predicates;

import com.giffing.bucket4j.spring.boot.starter.core.predicates.MethodExecutePredicate;
import jakarta.servlet.http.HttpServletRequest;

public class ServletMethodPredicate extends MethodExecutePredicate<HttpServletRequest> {

    @Override
    public boolean test(HttpServletRequest t) {
        return testRequestMethod(t.getMethod());
    }

}
