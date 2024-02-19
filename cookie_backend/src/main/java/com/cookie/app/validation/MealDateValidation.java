package com.cookie.app.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MealDateValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface MealDateValidation {
    String message() default "Meal date must be set in future";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
