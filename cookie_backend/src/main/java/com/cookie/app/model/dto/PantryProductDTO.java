package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Category;
import jakarta.validation.constraints.NotEmpty;
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
//        @NotEmpty(message = "Quantity cannot be empty")
        String quantity,
        String placement
) {}
