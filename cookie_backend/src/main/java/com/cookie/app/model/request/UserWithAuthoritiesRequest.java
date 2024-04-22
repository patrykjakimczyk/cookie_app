package com.cookie.app.model.request;

import com.cookie.app.model.enums.AuthorityEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record UserWithAuthoritiesRequest(
        @Positive(message = "User id must be greater than 0")
        Long userId,

        @NotEmpty(message = "Set of authorities cannot be empty")
        Set<AuthorityEnum> authorities
) {}
