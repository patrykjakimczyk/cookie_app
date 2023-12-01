package com.cookie.app.model.request;

import jakarta.validation.constraints.Min;

public record AddUserToGroupRequest(
        @Min(1)
        Long userId
) {}
