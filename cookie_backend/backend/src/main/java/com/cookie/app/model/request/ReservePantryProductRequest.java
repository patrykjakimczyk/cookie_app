package com.cookie.app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ReservePantryProductRequest(
        @Schema(example = "20")
        @NotNull(message = "Reserved quantity must be present")
        Integer reserved
) {}
