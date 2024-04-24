package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.util.ImageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
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
                recipe.getMealType(),
                recipe.getCuisine(),
                recipe.getPortions(),
                ImageUtil.decompressImage(recipe.getRecipeImage()),
                recipe.getCreator().getUsername(),
                recipe.getRecipeProducts()
                        .stream()
                        .map(recipeProductMapperDTO::apply)
                        .toList()
        );
    }
}
