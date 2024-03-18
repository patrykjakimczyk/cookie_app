package com.cookie.app.model.dto;

public record ShoppingListDTO(
        long listId,
        String listName,
        int nrOfProducts,
        int nrOfPurchasedProducts,
        long groupId,
        String groupName
) {}
