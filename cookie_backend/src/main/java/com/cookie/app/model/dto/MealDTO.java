package com.cookie.app.model.dto;

import java.sql.Timestamp;

public record MealDTO(
        Timestamp mealDate,
        UserDTO user,
        GroupDTO group,
        RecipeDTO recipe
) {}
