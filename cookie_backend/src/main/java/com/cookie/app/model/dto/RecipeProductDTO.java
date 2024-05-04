package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Unit;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record RecipeProductDTO(
        @Schema(example = "1")
        @NotNull(message = "Recipe product id must be present")
        @PositiveOrZero(message = "Recipe product id must be equal or greater than 0")
        Long id,

        @NotNull(message = "Recipe product must be present")
        @Valid
        ProductDTO product,

        @Schema(example = "100")
        @NotNull(message = "Quantity must be present")
        @PositiveOrZero(message = "Quantity must be equal or greater than 1")
        Integer quantity,

        @Schema(example = "GRAMS")
        @NotNull(message = "Unit must be present")
        Unit unit
) {
}
