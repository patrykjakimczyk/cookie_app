package com.cookie.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.sql.Timestamp;
import java.time.Instant;

public class MealDateValidator implements ConstraintValidator<MealDateValidation, Timestamp> {

    @Override
    public boolean isValid(Timestamp timestamp, ConstraintValidatorContext constraintValidatorContext) {
        return timestamp.after(Timestamp.from(Instant.now()));
    }
}