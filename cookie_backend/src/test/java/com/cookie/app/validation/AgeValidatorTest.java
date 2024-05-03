package com.cookie.app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AgeValidatorTest {
    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    Clock clock = Mockito.mock(Clock.class);
    AgeValidator ageValidator = new AgeValidator(clock);

    @BeforeEach
    void init() {
        doReturn(Instant.MAX).when(clock).instant();
    }

    @Test
    void test_isValidReturnsTrueFor15YearsOld() {
        final long fifteenYears = 473353890000L;

        assertTrue(ageValidator.isValid(Timestamp.from(Instant.MAX.minusMillis(fifteenYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrue13Years() {
        final long thirteenYears = 410240376000L;


        assertTrue(ageValidator.isValid(Timestamp.from(Instant.MAX.minusMillis(thirteenYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsFalseForAlmost13Years() {
        final long almostThirteenYears = 410240375999L;

        assertFalse(ageValidator.isValid(Timestamp.from(Instant.MAX.minusMillis(almostThirteenYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrueFor125Years() {
        final long hundredtwentyfiveYears = 3944619000000L;

        assertTrue(ageValidator.isValid(Timestamp.from(Instant.MAX.minusMillis(hundredtwentyfiveYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsFalseForOver125Years() {
        final long overHundredtwentyfiveYears = 3944619000001L;

        assertFalse(ageValidator.isValid(Timestamp.from(Instant.MAX.minusMillis(overHundredtwentyfiveYears)), constraintValidatorContext));
    }
}
