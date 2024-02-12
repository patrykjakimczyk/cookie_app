package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.util.ImageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Component
public class RecipeMapperDTO implements Function<Recipe, RecipeDTO> {
    @Override
    public RecipeDTO apply(Recipe recipe) {
        return new RecipeDTO(
                recipe.getId(),
                recipe.getRecipeName(),
                recipe.getPreparationTime(),
                recipe.getMealType(),
                recipe.getCuisine(),
                recipe.getPortions(),
                ImageUtil.decompressImage(recipe.getRecipeImage()),
                recipe.getCreator().getUsername(),
                recipe.getRecipeProducts().size()
        );
    }
}
