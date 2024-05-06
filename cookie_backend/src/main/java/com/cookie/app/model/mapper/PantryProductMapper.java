package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.entity.PantryProduct;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(
        componentModel = "spring",
        uses = {ProductMapper.class},
        injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface PantryProductMapper {

    PantryProductDTO mapToDto(PantryProduct entity);
}
