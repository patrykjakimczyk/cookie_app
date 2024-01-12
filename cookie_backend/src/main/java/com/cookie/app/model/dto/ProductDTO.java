package com.cookie.app.model.dto;


import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.enums.Category;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ProductDTO {
    @NotNull(message = "Shopping list product id must be present")
    @Min(value = 0, message = "Shopping list product id must be equal or greater than 0")
    private final Long id;

    @Pattern(
            regexp = RegexConstants.PRODUCT_NAME_REGEX,
            message = "Product name can only contains letters, digits, whitespaces and its length has to be between 3 and 50"
    )
    private final String productName;
    private final Category category;
}
