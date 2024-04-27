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
class AgeValidatorTest {
    AgeValidator ageValidator = new AgeValidator();
    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    @Test
    void test_isValidReturnsTrueFor15YearsOld() {
        final long fifteenYears = 473353890000L;

        assertTrue(ageValidator.isValid(Timestamp.from(Instant.now().minusMillis(fifteenYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrue13Years() {
        final long thirteenYears = 410240386000L;

        assertTrue(ageValidator.isValid(Timestamp.from(Instant.now().minusMillis(thirteenYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsFalseForAlmost13Years() {
        final long almostThirteenYears = 410240366000L;

        assertFalse(ageValidator.isValid(Timestamp.from(Instant.now().minusMillis(almostThirteenYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsTrueFor125Years() {
        final long hundredtwentyfiveYears = 3944618900000L;

        assertTrue(ageValidator.isValid(Timestamp.from(Instant.now().minusMillis(hundredtwentyfiveYears)), constraintValidatorContext));
    }

    @Test
    void test_isValidReturnsFalseForLittleOver125Years() {
        final long overHundredtwentyfiveYears = 3944619900000L;

        assertFalse(ageValidator.isValid(Timestamp.from(Instant.now().minusMillis(overHundredtwentyfiveYears)), constraintValidatorContext));
    }
}
