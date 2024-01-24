package com.cookie.app.model.dto;

public record RecipeDTO(
        long id,
        String recipeName,
        int preparationTime,
        String cuisine,
        int portions,
        byte[] recipeImage,
        String creatorUserName,
        int nrOfProducts
) {}
