package com.cookie.app.model.request;

import jakarta.validation.constraints.NotNull;

public record ReservePantryProductRequest(
        @NotNull(message = "Reserved quantity must be present")
        Integer reserved
) {}
