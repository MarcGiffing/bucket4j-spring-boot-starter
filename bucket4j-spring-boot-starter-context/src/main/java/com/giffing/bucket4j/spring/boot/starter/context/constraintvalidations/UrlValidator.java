package com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations;

import com.giffing.bucket4j.spring.boot.starter.context.UrlMapper;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class UrlValidator
        implements ConstraintValidator<ValidUrl, String> {

    private final UrlMapper urlMapper;

    @Autowired
    public UrlValidator(UrlMapper urlMapper) {
        this.urlMapper = urlMapper;
    }

    @Override
    public boolean isValid(
            String value,
            ConstraintValidatorContext context) {
        try {
            this.urlMapper.isValid(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
