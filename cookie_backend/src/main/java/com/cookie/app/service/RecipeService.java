package com.cookie.app.service;

import com.cookie.app.model.dto.RecipeDTO;
import org.springframework.data.domain.Page;

public interface RecipeService {
    Page<RecipeDTO> getRecipes(int page, String filterValue, String sortColName, String sortDirection, String userEmail);
}
