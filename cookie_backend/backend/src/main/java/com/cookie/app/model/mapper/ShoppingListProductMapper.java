package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.ShoppingListProduct;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {ProductMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ShoppingListProductMapper {

    ShoppingListProductDTO mapToDto(ShoppingListProduct entity);
}
