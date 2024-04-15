package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

public record PantryProductDTO (
        @NotNull(message = "Pantry product id must be present")
        @PositiveOrZero(message = "Pantry product id must be equal or greater than 0")
        Long id,
        @Valid
        ProductDTO product,
        Timestamp purchaseDate,
        Timestamp expirationDate,
        @NotNull(message = "Quantity must be present and positive number")
        @PositiveOrZero(message = "Quantity must be equal or greater than 1")
        int quantity,
        Unit unit,
        @NotNull(message = "Reserved count must be present")
        @PositiveOrZero(message = "Reserved count must be equal or greater than 0")
        int reserved,
        @Pattern(
                regexp = RegexConstants.PLACEMENT_REGEX,
                message = "Placement can only contains letters, digits, whitespaces and its length cannnot be greater than 30"
        )
        String placement
) {

}
