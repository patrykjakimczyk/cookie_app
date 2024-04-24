package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreatePantryRequest(
        @NotNull(message = "Pantry name must be present")
        @Pattern(
                regexp = RegexConstants.PANTRY_NAME_REGEX,
                message = "Pantry name can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 3 and 30"
        )
        String pantryName,
        @NotNull(message = "Group id must be present")
        @Positive(message = "Group id must be greater than 0")
        Long groupId
) {}
