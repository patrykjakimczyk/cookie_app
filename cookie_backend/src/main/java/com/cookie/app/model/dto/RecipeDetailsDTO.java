package com.cookie.app.model.dto;

import com.cookie.app.model.enums.MealType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record RecipeDetailsDTO(
        @Schema(example = "1")
        long id,

        @Schema(example = "Spaghetti")
        String recipeName,

        String preparation,

        @Schema(example = "30")
        int preparationTime,

        @Schema(example = "DINNER")
        MealType mealType,

        @Schema(example = "Italian")
        String cuisine,

        @Schema(example = "4")
        int portions,
        byte[] recipeImage,

        @Schema(example = "Username")
        String creatorUserName,
        List<RecipeProductDTO> products
) {}
