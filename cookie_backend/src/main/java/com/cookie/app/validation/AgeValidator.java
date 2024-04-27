package com.cookie.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AgeValidator implements ConstraintValidator<AgeValidation, Timestamp> {
    private static final long MIN_AGE_IN_MILLIS = 410240376000L; // 13 years
    private static final long MAX_AGE_IN_MILLIS = 3944619000000L; // 125 years

    @Override
    public boolean isValid(Timestamp timestamp, ConstraintValidatorContext constraintValidatorContext) {
        return timestamp.after(Timestamp.from(Instant.now().minusMillis(MAX_AGE_IN_MILLIS))) &&
                timestamp.before(Timestamp.from(Instant.now().minusMillis(MIN_AGE_IN_MILLIS)));
    }
}
