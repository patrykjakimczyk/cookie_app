package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Unit;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ShoppingListProductDTO (
        @NotNull(message = "Shopping list id must be present")
        @PositiveOrZero(message = "Shopping list id must be equal or greater than 0")
        Long id,
        @Valid
        ProductDTO product,
        @NotNull(message = "Quantity must be present")
        @PositiveOrZero(message = "Quantity must be equal or greater than 1")
        int quantity,
        Unit unit,
        boolean purchased
) {
}
