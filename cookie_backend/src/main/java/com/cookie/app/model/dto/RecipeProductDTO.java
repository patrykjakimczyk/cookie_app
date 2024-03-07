package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecipeProductDTO(
        @NotNull(message = "Recipe product id must be present")
        @Min(value = 0, message = "Recipe product id must be equal or greater than 0")
        Long id,
        @Valid
        ProductDTO product,
        @NotNull(message = "Quantity must be present")
        @Min(value = 0, message = "Quantity must be equal or greater than 1")
        int quantity,
        Unit unit
) {
}
