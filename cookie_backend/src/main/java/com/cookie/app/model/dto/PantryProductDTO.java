package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.sql.Timestamp;

public record PantryProductDTO(
        Long id,
        @Pattern(
                regexp = RegexConstants.PRODUCT_NAME_REGEX,
                message = "Product name can only contains those symbols (a-z, A-Z, 0-9, whitespace) and its length has to be between 5 and 50"
        )
        String productName,
        Category category,
        Timestamp purchaseDate,
        Timestamp expirationDate,
        @NotNull(message = "Quantity must be a positive number")
        @Min(value = 0, message = "Quantity must be equal or greater than 1")
        int quantity,
        Unit unit,
        @NotNull(message = "Reserved count must be present")
        @Min(value = 0, message = "Reserved count must be equal or greater than 0")
        int reserved,
        String placement
) {}
