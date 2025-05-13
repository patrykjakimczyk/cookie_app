package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.RecipeProductDTO;
import com.cookie.app.model.enums.MealType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateRecipeRequest(
        @Pattern(
                regexp = RegexConstants.RECIPE_NAME_REGEX,
                message = "Recipe name can only contains letters, digits, whitespaces and its length has to be between 5 and 60"
        )
        @NotNull(message = "Recipe name must be present")
        String recipeName,
        @Pattern(
                regexp = RegexConstants.PREPARATION_REGEX,
                message = "Recipe name can only contains letters, digits, whitespaces and its length has to be between 5 and 512"
        )
        @NotNull(message = "Preparation must be present")
        String preparation,
        @NotNull(message = "Preparation time must be present")
        @Min(value = 5, message = "Preparation time must be greater than 5")
        @Max(value = 2880 ,message = "Preparation time must be less than 12")
        Integer preparationTime,
        @NotNull(message = "Meal type must be present")
        MealType mealType,
        @Pattern(
                regexp = RegexConstants.CUISINE_REGEX,
                message = "Cuisine name can only contains letters, digits, whitespaces and its length has to be between 4 and 30"
        )
        String cuisine,
        @NotNull(message = "Nr of portions must be present")
        @Positive(message = "Nr of portions must be greater than 5")
        @Max(value = 12 ,message = "Nr of portions must be less than 12")
        Integer portions,
        @NotNull(message = "UpdateImage must be present")
        Boolean updateImage,
        @NotNull(message = "Products list must be present")
        @NotEmpty(message = "Nr of products must be at least 1")
        List<@Valid RecipeProductDTO> products
) {}
