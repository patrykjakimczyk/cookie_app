package com.cookie.app.model.dto;

public record RecipeDTO(
        String recipeName,
        int preparationTime,
        String cuisine,
        int portions,
        String creatorUserName,
        int nrOfProducts
) {}
