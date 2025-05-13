package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeDTO;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.util.ImageUtil;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface RecipeMapper {

    @Mapping(source = "entity", target = "recipeImage", qualifiedByName = "decompressedImage")
    @Mapping(source = "entity.creator.username", target = "creatorUserName")
    @Mapping(source = "entity", target = "nrOfProducts", qualifiedByName = "nrOfProducts")
    RecipeDTO mapToDto(Recipe entity);

    @Named("decompressedImage")
    default byte[] mapToDecompressedImage(Recipe recipe) {
        return ImageUtil.decompressImage(recipe.getRecipeImage());
    }

    @Named("nrOfProducts")
    default int mapToNrOfProducts(Recipe recipe) {
        return recipe.getRecipeProducts().size();
    }
}
