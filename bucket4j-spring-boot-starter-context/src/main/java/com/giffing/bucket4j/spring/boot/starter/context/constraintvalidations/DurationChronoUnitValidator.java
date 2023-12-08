package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import java.time.temporal.ChronoUnit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DurationChronoUnitValidator implements ConstraintValidator<ValidDurationChronoUnit, ChronoUnit> {

	@Override
	public boolean isValid(ChronoUnit value, ConstraintValidatorContext context) {
		return value != null && (value == ChronoUnit.DAYS || !value.isDurationEstimated());
	}
}