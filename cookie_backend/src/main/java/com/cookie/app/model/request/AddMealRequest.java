package com.cookie.app.model.request;

import com.cookie.app.validation.MealDateValidation;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.sql.Timestamp;

public record AddMealRequest(
        @NotNull(message = "Meal date cannot be null")
        @MealDateValidation
        Timestamp mealDate,

        @Min(value = 1, message = "Group id must be greater than 0")
        long groupId,

        @Min(value = 1, message = "Recipe id must be greater than 0")
        long recipeId
) {}
