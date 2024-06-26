package com.cookie.app.model.dto;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductDTO(
        @Schema(example = "1")
        @NotNull(message = "Product id must be present")
        @PositiveOrZero(message = "Product id must be equal or greater than 0")
        Long productId,

        @Schema(example = "Flour")
        @NotNull(message = "Product name must be present")
        @Pattern(regexp = RegexConstants.PRODUCT_NAME_REGEX,
                message = "Product name can only contains letters, digits, whitespaces and its length has to be between 3 and 50")
        String productName,

        @Schema(example = "BAKING_GOODS")
        @NotNull(message = "Category must be present")
        Category category
) {
}
