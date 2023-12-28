package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ShoppingListDTO;
import com.cookie.app.model.entity.ShoppingList;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class ShoppingListMapperDTO implements Function<ShoppingList, ShoppingListDTO> {
    @Override
    public ShoppingListDTO apply(ShoppingList shoppingList) {
        return new ShoppingListDTO(
                shoppingList.getId(),
                shoppingList.getListName(),
                shoppingList.getListProducts().size(),
                shoppingList.getGroup().getId(),
                shoppingList.getGroup().getGroupName(),
                shoppingList.isPurchased()
        );
    }
}
