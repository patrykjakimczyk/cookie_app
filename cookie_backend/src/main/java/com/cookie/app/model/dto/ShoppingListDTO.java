package com.cookie.app.model.dto;

public record ShoppingListDTO(
        long listId,
        String listName,
        int nrOfProducts,
        long groupId,
        String groupName,
        boolean purchased
) {}
