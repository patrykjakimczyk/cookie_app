package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeDetailsDTO;
import com.cookie.app.model.entity.Recipe;
import com.cookie.app.util.ImageUtil;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        uses = {RecipeProductMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface RecipeDetailsMapper {

    @Mapping(source = "entity", target = "recipeImage", qualifiedByName = "decompressedImage")
    @Mapping(source = "entity.recipeProducts", target = "products")
    RecipeDetailsDTO mapToDto(Recipe entity);

    @Named("decompressedImage")
    default byte[] mapToDecompressedImage(Recipe recipe) {
        return ImageUtil.decompressImage(recipe.getRecipeImage());
    }
}
