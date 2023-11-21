package com.cookie.app.model.request;

import jakarta.validation.constraints.Min;

public record ReservePantryProductRequest(
        @Min(
                value = 1,
                message = "Invalid email provided"
        )
        int reserved
) {}
