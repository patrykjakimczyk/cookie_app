package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.entity.Recipe;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class RecipeMapperDTO implements Function<Recipe, RecipeDTO> {
    @Override
    public RecipeDTO apply(Recipe recipe) {
        return new RecipeDTO(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getPreparationTime(),
                recipe.getCuisine(),
                recipe.getPortions(),
                recipe.getCreator().getUsername(),
                recipe.getRecipeProducts().size()
        );
    }
}
