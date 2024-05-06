package com.cookie.app.model.dto;

import java.time.LocalDateTime;

public record MealDTO(
        long id,
        LocalDateTime mealDate,
        String username,
        GroupDTO group,
        RecipeDTO recipe
) {}
