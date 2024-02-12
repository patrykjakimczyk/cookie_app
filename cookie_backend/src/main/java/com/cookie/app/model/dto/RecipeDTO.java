package com.cookie.app.model.dto;

import com.cookie.app.model.enums.MealType;

public record RecipeDTO(
        long id,
        String recipeName,
        int preparationTime,
        MealType mealType,
        String cuisine,
        int portions,
        byte[] recipeImage,
        String creatorUserName,
        int nrOfProducts
) {}
