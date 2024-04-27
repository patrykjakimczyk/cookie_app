package com.cookie.app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class MealDateValidatorTest {
    MealDateValidator mealDateValidator = new MealDateValidator();
    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    @Test
    void test_isValidReturnsFalseForYesterdayDate() {
        final Timestamp yesterday = Timestamp.from(Instant.now().minusSeconds(86400));

        assertFalse(mealDateValidator.isValid(yesterday, constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrueForTomorrowDate() {
        final Timestamp tommorow = Timestamp.from(Instant.now().plusSeconds(86400));

        assertTrue(mealDateValidator.isValid(tommorow, constraintValidatorContext));
    }
}
