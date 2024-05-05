package com.cookie.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;

@RequiredArgsConstructor
public class AgeValidator implements ConstraintValidator<AgeValidation, LocalDate> {
    private static final long MIN_AGE = 13L;
    private static final long MAX_AGE = 125L; // 125 years

    private final Clock clock;

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext constraintValidatorContext) {
        return (date.isAfter(LocalDate.now(clock).minusYears(MAX_AGE)) ||
                date.isEqual(LocalDate.now(clock).minusYears(MAX_AGE))) &&
                (date.isBefore(LocalDate.now(clock).minusYears(MIN_AGE)) ||
                date.isEqual(LocalDate.now(clock).minusYears(MIN_AGE)));
    }
}
