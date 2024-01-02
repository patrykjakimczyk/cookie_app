package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ShoppingListProductDTO(
        Long id,
        @Pattern(
                regexp = RegexConstants.PRODUCT_NAME_REGEX,
                message = "Product name can only contains those symbols (a-z, A-Z, 0-9, whitespace) and its length has to be between 5 and 50"
        )
        String productName,
        Category category,
        @NotNull(message = "Quantity must be a positive number")
        @Min(value = 0, message = "Quantity must be equal or greater than 1")
        int quantity,
        Unit unit,
        boolean purchased
) {}
