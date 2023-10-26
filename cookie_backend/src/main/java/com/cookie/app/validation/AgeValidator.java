package com.cookie.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.sql.Timestamp;
import java.time.Instant;


public class AgeValidator implements ConstraintValidator<AgeValidation, Timestamp> {
    private static final long AGE_IN_MILLIS = 410240376000L;

    @Override
    public boolean isValid(Timestamp timestamp, ConstraintValidatorContext constraintValidatorContext) {
        return timestamp.before(Timestamp.from(Instant.now().minusMillis(AGE_IN_MILLIS)));
    }
}
