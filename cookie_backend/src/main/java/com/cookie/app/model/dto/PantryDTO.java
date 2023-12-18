package com.cookie.app.model.dto;

public record PantryDTO(
        long pantryId,
        String pantryName,
        int nrOfProducts,
        long groupId,
        String groupName
) {}
