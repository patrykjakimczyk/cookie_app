package com.cookie.app.model.dto;

import java.sql.Timestamp;

public record MealDTO(
        long id,
        Timestamp mealDate,
        String username,
        GroupDTO group,
        RecipeDTO recipe
) {}
