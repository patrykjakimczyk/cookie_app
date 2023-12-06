package com.cookie.app.model.request;

import com.cookie.app.model.enums.AuthorityEnum;
import jakarta.validation.constraints.Min;

import java.util.Set;

public record AssignAuthoritiesToUserRequest(
        @Min(value = 1, message = "User id must be greater than 0")
        Long userId,

        Set<AuthorityEnum> authoritiesToAssign
) {}
