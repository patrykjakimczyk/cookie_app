package com.cookie.app.model.dto;

import java.util.List;

public record RecipeDetailsDTO(
        long id,
        String recipeName,
        String preparation,
        int preparationTime,
        String cuisine,
        int portions,
        String creatorName,
        List<RecipeProductDTO> products
) {}
