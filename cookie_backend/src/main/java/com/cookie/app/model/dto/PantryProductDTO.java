package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

public record PantryProductDTO (
        @NotNull(message = "Pantry product id must be present")
        @PositiveOrZero(message = "Pantry product id must be equal or greater than 0")
        Long id,
        @NotNull(message = "Product must be present")
        @Valid
        ProductDTO product,
        Timestamp purchaseDate,
        Timestamp expirationDate,
        @NotNull(message = "Quantity must be present")
        @PositiveOrZero(message = "Quantity must be equal or greater than 1")
        Integer quantity,
        @NotNull(message = "Unit must be present")
        Unit unit,
        @NotNull(message = "Reserved count must be present")
        @PositiveOrZero(message = "Reserved count must be equal or greater than 0")
        Integer reserved,
        @Pattern(
                regexp = RegexConstants.PLACEMENT_REGEX,
                message = "Placement can only contains letters, digits, whitespaces and its length cannnot be greater than 30"
        )
        String placement
) {

}
