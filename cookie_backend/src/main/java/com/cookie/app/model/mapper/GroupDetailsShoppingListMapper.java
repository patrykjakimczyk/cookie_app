package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDetailsShoppingListDTO;
import com.cookie.app.model.entity.ShoppingList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GroupDetailsShoppingListMapper {

    @Mapping(source = "entity.id", target = "listId")
    GroupDetailsShoppingListDTO mapToDto(ShoppingList entity);

    List<GroupDetailsShoppingListDTO> mapToDtoList(List<ShoppingList> entities);

}
