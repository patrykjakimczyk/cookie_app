package com.cookie.app.model.request;

import com.cookie.app.model.enums.Gender;
import com.cookie.app.model.RegexConstants;
import com.cookie.app.validation.AgeValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.sql.Timestamp;

public record RegistrationRequest(
        @Schema(example = "username")
        @NotNull(message = "Username must be present")
        @Pattern(
                regexp = RegexConstants.USERNAME_REGEX,
                message = "Username can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 6 and 30"
        )
        String username,
        @Schema(example = "email@email.com")
        @NotNull(message = "E-mail must be present")
        @Pattern(
                regexp = RegexConstants.EMAIL_REGEX,
                message = "Invalid email provided"
        )
        String email,
        @Schema(example = "ZAQ!2wsx")
        @NotNull(message = "Password must be present")
        @Pattern(
                regexp = RegexConstants.PASSWORD_REGEX,
                message = "Password has to contain those symbols at least one time for each one: (a-z, A-Z, 0-9, '@$!%*?&') and its length has to be between 8 and 128"
        )
        String password,
        @NotNull(message = "Birth date must be present")
        @AgeValidation
        Timestamp birthDate,
        @Schema(example = "FEMALE")
        Gender gender
) {}
