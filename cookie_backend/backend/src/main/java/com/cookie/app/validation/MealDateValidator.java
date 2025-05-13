package com.cookie.app.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDateTime;

@RequiredArgsConstructor
public class MealDateValidator implements ConstraintValidator<MealDateValidation, LocalDateTime> {
    private final Clock clock;

    @Override
    public boolean isValid(LocalDateTime date, ConstraintValidatorContext constraintValidatorContext) {
        return date.isAfter(LocalDateTime.now(clock));
    }
}