package com.cookie.app.model.request;


import com.cookie.app.model.enums.Gender;
import com.cookie.app.model.RegexConstants;
import com.cookie.app.validation.AgeValidation;
import jakarta.validation.constraints.*;

import java.sql.Timestamp;

public record RegistrationRequest(
        @NotNull(message = "Username cannot be null")
        @Pattern(
                regexp = RegexConstants.USERNAME_REGEX,
                message = "Username can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 6 and 30"
        )
        String username,

        @NotNull(message = "Email cannot be null")
        @Pattern(
                regexp = RegexConstants.EMAIL_REGEX,
                message = "Invalid email provided"
        )
        String email,

        @NotNull(message = "Password cannot be null")
        @Pattern(
                regexp = RegexConstants.PASSWORD_REGEX,
                message = "Password has to contain those symbols at least one time for each one: (a-z, A-Z, 0-9, '@$!%*?&') and its length has to be between 8 and 128"
        )
        String password,

        @NotNull(message = "Birth date cannot be null")
        @AgeValidation
        Timestamp birthDate,

        Gender gender
) {}
