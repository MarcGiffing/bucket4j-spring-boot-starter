package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import java.time.temporal.ChronoUnit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * This validator is used to check if a ChronoUnit is accepted by the Duration.of() method.
 *
 * The Duration.of() method does not allow estimated time units, but DAYS is an exception to this rule.
 * ChronoUnit treats Days as estimated values because of daylight savings, while Duration.of() treats it
 * as an exact value of 24 hours. For this reason the validator also allows Days as valid value.
 *
 */
public class DurationChronoUnitValidator implements ConstraintValidator<ValidDurationChronoUnit, ChronoUnit> {

	@Override
	public boolean isValid(ChronoUnit value, ConstraintValidatorContext context) {
		return value != null && (value == ChronoUnit.DAYS || !value.isDurationEstimated());
	}
}