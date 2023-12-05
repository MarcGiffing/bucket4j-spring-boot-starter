package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import com.giffing.bucket4j.spring.boot.starter.context.properties.BandWidth;
import com.giffing.bucket4j.spring.boot.starter.context.properties.RateLimit;
import io.github.bucket4j.TokensInheritanceStrategy;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class RateLimitBandWidthIdsValidator implements ConstraintValidator<ValidBandWidthIds, RateLimit> {

	@Override
	public boolean isValid(RateLimit rateLimit, ConstraintValidatorContext context) {
		Set<String> idSet = new HashSet<>();

		for (BandWidth bandWidth : rateLimit.getBandwidths()) {
			String id = bandWidth.getId();

			if(id == null && rateLimit.getTokensInheritanceStrategy() == TokensInheritanceStrategy.RESET) {
				continue;
			}

			if (!idSet.add(id)) {
				String errorMessage = (id == null)
						? "Multiple bandwidths without id detected. This is only allowed when TokenInheritanceStrategy 'RESET' is applied."
						: String.format("Duplicate bandwidth id: %s", id);

				context.disableDefaultConstraintViolation();
				context.buildConstraintViolationWithTemplate(errorMessage).addConstraintViolation();
				return false;
			}
		}
		return true;
	}
}
