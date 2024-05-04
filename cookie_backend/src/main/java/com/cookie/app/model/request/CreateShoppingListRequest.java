package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record CreateShoppingListRequest(
        @Schema(example = "listname")
        @NotNull(message = "Shoppin list name must be present")
        @Pattern(
                regexp = RegexConstants.SHOPPING_LIST_NAME_REGEX,
                message = "Shopping list name can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 3 and 30"
        )
        String shoppingListName,

        @Schema(example = "1")
        @NotNull(message = "Group id must be present")
        @Positive(message = "Group id must be greater than 0")
        Long groupId
) {}
