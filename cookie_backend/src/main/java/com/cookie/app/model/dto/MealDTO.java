package com.cookie.app.model.dto;

import java.sql.Timestamp;

public record MealDTO(
        long id,
        Timestamp mealDate,
        UserDTO user,
        GroupDTO group,
        RecipeDTO recipe
) {}
