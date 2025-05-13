package com.cookie.app.model.request;

import com.cookie.app.validation.MealDateValidation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record AddMealRequest(
        @NotNull(message = "Meal must be present")
        @MealDateValidation
        LocalDateTime mealDate,

        @Schema(example = "1")
        @NotNull(message = "Group id must be present")
        @Positive(message = "Group id must be greater than 0")
        Long groupId,

        @Schema(example = "1")
        @NotNull(message = "Recipe id must be present")
        @Positive(message = "Recipe id must be greater than 0")
        Long recipeId
) {
}
