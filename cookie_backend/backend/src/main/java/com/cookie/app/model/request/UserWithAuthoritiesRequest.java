package com.cookie.app.model.request;

import com.cookie.app.model.enums.AuthorityEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record UserWithAuthoritiesRequest(
        @NotNull(message = "User id must be present")
        @Positive(message = "User id must be greater than 0")
        Long userId,
        @NotNull(message = "Set of authorities must be present")
        @NotEmpty(message = "Set of authorities cannot be empty")
        Set<AuthorityEnum> authorities
) {}
