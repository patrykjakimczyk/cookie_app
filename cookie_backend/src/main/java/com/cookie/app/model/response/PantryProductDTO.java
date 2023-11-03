package com.cookie.app.model.response;

import com.cookie.app.model.enums.Category;

import java.sql.Timestamp;

public record PantryProductDTO(
        Long id,
        String productName,
        Category category,
        Timestamp purchaseDate,
        Timestamp expirationDate,
        String quantity,
        String placement
) {}
