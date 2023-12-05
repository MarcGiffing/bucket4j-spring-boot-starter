package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Constraint(validatedBy = Bucket4JConfigurationPredicateNameValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface ValidPredicateNames {
	String message() default "Invalid predicate name";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
