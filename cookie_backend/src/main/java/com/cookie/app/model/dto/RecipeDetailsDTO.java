package com.cookie.app.model.dto;

import com.cookie.app.model.enums.MealType;

import java.util.List;

public record RecipeDetailsDTO(
        long id,
        String recipeName,
        String preparation,
        int preparationTime,
        MealType mealType,
        String cuisine,
        int portions,
        byte[] recipeImage,
        String creatorUserName,
        List<RecipeProductDTO> products
) {}
