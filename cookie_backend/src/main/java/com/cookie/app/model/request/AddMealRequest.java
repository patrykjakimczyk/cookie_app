package com.cookie.app.model.request;

import com.cookie.app.validation.MealDateValidation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.sql.Timestamp;

public record AddMealRequest(
        @NotNull(message = "Meal must be present")
        @MealDateValidation
        Timestamp mealDate,
        @NotNull(message = "Group id must be present")
        @Positive(message = "Group id must be greater than 0")
        Long groupId,
        @NotNull(message = "Recipe id must be present")
        @Positive(message = "Recipe id must be greater than 0")
        Long recipeId
) {}
