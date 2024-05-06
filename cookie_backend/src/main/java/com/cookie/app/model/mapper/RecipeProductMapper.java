package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.RecipeProductDTO;
import com.cookie.app.model.entity.RecipeProduct;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {ProductMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface RecipeProductMapper {

    RecipeProductDTO mapToDto(RecipeProduct entity);

    List<RecipeProductDTO> mapToDtoList(List<RecipeProduct> entities);
}
