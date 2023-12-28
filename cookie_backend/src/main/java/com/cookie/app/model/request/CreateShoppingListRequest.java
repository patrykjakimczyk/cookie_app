package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record CreateShoppingListRequest(
        @Pattern(
                regexp = RegexConstants.SHOPPING_LIST_NAME_REGEX,
                message = "Shopping list name can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 3 and 30"
        )
        String shoppingListName,

        @Min(1)
        long groupId
) {}
