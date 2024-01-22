package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.entity.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@RequiredArgsConstructor
@Component
public class RecipeDetailsMapperDTO implements Function<Recipe, RecipeDetailsDTO> {
    private final RecipeProductMapperDTO recipeProductMapperDTO;

    @Override
    public RecipeDetailsDTO apply(Recipe recipe) {
        return new RecipeDetailsDTO(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getPreparation(),
                recipe.getPreparationTime(),
                recipe.getCuisine(),
                recipe.getPortions(),
                recipe.getCreator().getUsername(),
                recipe.getRecipeProducts()
                        .stream()
                        .map(recipeProductMapperDTO::apply)
                        .toList()
        );
    }
}
