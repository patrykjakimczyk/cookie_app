package com.cookie.app.model.request;


import com.cookie.app.model.enums.Gender;
import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Role;
import jakarta.validation.constraints.*;

import java.time.Instant;

public record RegistrationRequest(
        @NotNull
        Role role,

        @NotNull
        @Size(min = 6, max = 30)
        String username,

        @NotNull
        @NotBlank
        @Pattern(regexp = RegexConstants.EMAIL_REGEX)
        String email,

        @NotNull
        @NotBlank
        @Pattern(regexp = RegexConstants.PASSWORD_REGEX)
        String password,

        @NotNull
        Instant birthDate,

        @NotNull
        Gender gender
) {
}
