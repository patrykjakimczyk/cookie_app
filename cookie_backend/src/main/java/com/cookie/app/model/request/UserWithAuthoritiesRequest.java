package com.cookie.app.model.request;

import com.cookie.app.model.enums.AuthorityEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.util.Set;

public record UserWithAuthoritiesRequest(
        @Min(value = 1, message = "User id must be greater than 0")
        Long userId,

        @NotEmpty(message = "Set of authorities cannot be empty")
        Set<AuthorityEnum> authorities
) {}
