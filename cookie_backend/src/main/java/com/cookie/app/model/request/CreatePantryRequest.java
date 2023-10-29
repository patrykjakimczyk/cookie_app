package com.cookie.app.model.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePantryRequest(
        @NotNull(message = "Username cannot be null")
        @Size(min = 3, max = 30, message = "Pantry name's length must be between 3 and 30")
        String pantryName
) {}
