package com.cookie.app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MealDateValidatorTest {
    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    Clock clock = Mockito.mock(Clock.class);
    MealDateValidator mealDateValidator = new MealDateValidator(clock);

    @Test
    void test_isValidReturnsFalseForYesterdayDate() {
        final Instant currentInstant = Instant.now();
        final LocalDateTime yesterday = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC).minusSeconds(86400);

        doReturn(currentInstant).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        assertFalse(mealDateValidator.isValid(yesterday, constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrueForTomorrowDate() {
        final Instant currentInstant = Instant.now();
        final LocalDateTime tommorow = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC).plusSeconds(86400);

        doReturn(currentInstant).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        assertTrue(mealDateValidator.isValid(tommorow, constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrueSecondAfterCurrentTime() {
        final Instant currentInstant = Instant.now();
        final LocalDateTime tooLate = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC).plusSeconds(1);

        doReturn(currentInstant).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        assertTrue(mealDateValidator.isValid(tooLate, constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsFalseForSecondBeforeCurrentTime() {
        final Instant currentInstant = Instant.now();
        final LocalDateTime secondBefore = LocalDateTime.ofInstant(currentInstant, ZoneOffset.UTC).minusSeconds(1);

        doReturn(currentInstant).when(clock).instant();
        doReturn(ZoneOffset.UTC).when(clock).getZone();

        assertFalse(mealDateValidator.isValid(secondBefore, constraintValidatorContext));
    }
}
