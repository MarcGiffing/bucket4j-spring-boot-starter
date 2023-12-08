package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import java.lang.annotation.*;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DurationChronoUnitValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDurationChronoUnit {

	String message() default "Unsupported duration ChronoUnit. Only time based units are supported (NANOS to DAYS inclusive).";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}