package com.giffing.bucket4j.spring.boot.starter.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class RateLimitAopUtils {

    public static <R extends Annotation>  R getAnnotationFromMethodOrClass(Method method, Class<R> rateLimitingAnnotation) {
        R rateLimitAnnotation;
        if(method.getAnnotation(rateLimitingAnnotation) != null) {
            rateLimitAnnotation = method.getAnnotation(rateLimitingAnnotation);
        } else {
            rateLimitAnnotation = method.getDeclaringClass().getAnnotation(rateLimitingAnnotation);
        }
        return rateLimitAnnotation;
    }

}
