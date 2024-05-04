package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Unit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.sql.Timestamp;

public record PantryProductDTO (
        @Schema(example = "1")
        @NotNull(message = "Pantry product id must be present")
        @PositiveOrZero(message = "Pantry product id must be equal or greater than 0")
        Long id,

        @NotNull(message = "Product must be present")
        @Valid
        ProductDTO product,
        Timestamp purchaseDate,
        Timestamp expirationDate,

        @Schema(example = "100")
        @NotNull(message = "Quantity must be present")
        @PositiveOrZero(message = "Quantity must be equal or greater than 1")
        Integer quantity,

        @Schema(example = "GRAMS")
        @NotNull(message = "Unit must be present")
        Unit unit,

        @Schema(example = "10")
        @NotNull(message = "Reserved count must be present")
        @PositiveOrZero(message = "Reserved count must be equal or greater than 0")
        Integer reserved,

        @Schema(example = "Shelf")
        @Pattern(
                regexp = RegexConstants.PLACEMENT_REGEX,
                message = "Placement can only contains letters, digits, whitespaces and its length cannnot be greater than 30"
        )
        String placement
) {

}
