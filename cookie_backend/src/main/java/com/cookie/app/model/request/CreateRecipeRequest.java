package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.RecipeProductDTO;
import jakarta.validation.constraints.*;

import java.util.List;

public record CreateRecipeRequest(
        @NotNull(message = "Recipe id must be present")
        @Min(value = 0, message = "Recipe id must be equal or greater than 0")
        long id,

        @Pattern(
                regexp = RegexConstants.RECIPE_NAME_REGEX,
                message = "Recipe name can only contains letters, digits, whitespaces and its length has to be between 5 and 60"
        )
        String recipeName,

        @Pattern(
                regexp = RegexConstants.PREPARATION_REGEX,
                message = "Recipe name can only contains letters, digits, whitespaces and its length has to be between 5 and 512"
        )
        String preparation,

        @NotNull(message = "Preparation time must be present")
        @Min(value = 5, message = "Preparation time must be greater than 5")
        @Max(value = 2880 ,message = "Preparation time must be less than 12")
        int preparationTime,

//        @Pattern(
//                regexp = RegexConstants.CUISINE_REGEX,
//                message = "Cuisine name can only contains letters, digits, whitespaces and its length has to be between 4 and 30"
//        )
        String cuisine,

        @NotNull(message = "Nr of portions must be present")
        @Min(value = 1, message = "Nr of portions must be greater than 5")
        @Max(value = 12 ,message = "Nr of portions must be less than 12")
        int portions,

        @Pattern(
                regexp = RegexConstants.USERNAME_REGEX,
                message = "Creator username can only contains letters, digits, whitespaces and its length has to be between 6 and 30"
        )
        String creatorName,

        boolean updateImage,

        @Size(min = 1, message = "Nr of products must be at least 1")
        List<RecipeProductDTO> products
) {}
