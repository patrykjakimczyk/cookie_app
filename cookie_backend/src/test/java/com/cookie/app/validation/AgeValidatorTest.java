package com.cookie.app.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class AgeValidatorTest {
    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    Clock clock = Mockito.mock(Clock.class);
    AgeValidator ageValidator = new AgeValidator(clock);

    @BeforeEach
    void init() {
        doReturn(Instant.now()).when(clock).instant();
        doReturn(ZoneId.systemDefault()).when(clock).getZone();
    }

    @Test
    void test_isValidReturnsTrueFor15YearsOld() {
        final long fifteenYears = 15L;

        assertThat(ageValidator.isValid(LocalDate.now(clock).minusYears(fifteenYears), constraintValidatorContext)).isTrue();
    }

    @Test
    void test_isValidReturnsTrue13Years() {
        final long thirteenYears = 13L;

        assertThat(ageValidator.isValid(LocalDate.now(clock).minusYears(thirteenYears), constraintValidatorContext)).isTrue();
    }

    @Test
    void test_isValidReturnsFalseForAlmost13Years() {
        final long twelveYears = 12L;
        final long almostYear = 364L;

        assertThat(ageValidator.isValid(LocalDate.now(clock).minusYears(twelveYears).minusDays(almostYear), constraintValidatorContext)).isFalse();
    }

    @Test
    void test_isValidReturnsTrueFor125Years() {
        final long hundredtwentyfiveYears = 125L;

        assertThat(ageValidator.isValid(LocalDate.now(clock).minusYears(hundredtwentyfiveYears), constraintValidatorContext)).isTrue();
    }

    @Test
    void test_isValidReturnsFalseForOver125Years() {
        final long hundredtwentyfiveYears = 125L;

        assertThat(ageValidator.isValid(LocalDate.now(clock).minusYears(hundredtwentyfiveYears).minusDays(1), constraintValidatorContext)).isFalse();
    }
}