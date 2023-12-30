package com.cookie.app.model.dto;

import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;

public record ShoppingListProductDTO(
        Long id,
        String productName,
        Category category,
        int quantity,
        Unit unit,
        boolean purchased
) {}
