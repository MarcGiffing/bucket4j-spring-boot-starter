package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParseException;
import com.giffing.bucket4j.spring.boot.starter.context.UrlPatternParser;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UrlPatternValidator
        implements ConstraintValidator<ValidUrlPattern, String> {

    private final UrlPatternParser urlPatternParser;

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context) {
        try {
            this.urlPatternParser.parse(value);
            return true;
        } catch (UrlPatternParseException e) {
            return false;
        }
    }
}
