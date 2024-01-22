package com.cookie.app.service;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.dto.RecipeDetailsDTO;
import org.springframework.data.domain.Page;

public interface RecipeService {
    Page<RecipeDTO> getRecipes(int page, String filterValue, int prepTime, int portions, String sortColName, String sortDirection);
    RecipeDetailsDTO getRecipeDetails(long recipeId);
}
