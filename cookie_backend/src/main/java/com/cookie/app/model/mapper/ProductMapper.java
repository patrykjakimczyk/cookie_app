package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.entity.Product;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ProductMapper {

    @Mapping(source = "entity.id", target = "productId")
    ProductDTO mapToDto(Product entity);
}
