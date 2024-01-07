package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.GroupDetailsShoppingListDTO;
import com.cookie.app.model.entity.ShoppingList;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class GroupDetailsShoppingListMapperDTO implements Function<ShoppingList, GroupDetailsShoppingListDTO> {
    @Override
    public GroupDetailsShoppingListDTO apply(ShoppingList shoppingList) {
        return new GroupDetailsShoppingListDTO(shoppingList.getId(), shoppingList.getListName());
    }
}