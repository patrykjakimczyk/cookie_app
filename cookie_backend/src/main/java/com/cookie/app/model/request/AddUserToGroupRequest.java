package com.cookie.app.model.request;

import jakarta.validation.constraints.Min;

public record AddUserToGroupRequest(
        @Min(value = 1, message = "User id must be greater than 0")
        Long userId
) {}
