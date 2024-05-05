package com.cookie.app.model.mapper;

import com.cookie.app.model.dto.ShoppingListDTO;
import com.cookie.app.model.entity.ShoppingList;
import com.cookie.app.model.entity.ShoppingListProduct;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ShoppingListMapper {

    @Mapping(source = "entity.id", target = "listId")
    @Mapping(source = "entity", target = "nrOfProducts", qualifiedByName = "nrOfProducts")
    @Mapping(source = "entity", target = "nrOfPurchasedProducts", qualifiedByName = "nrOfPurchasedProducts")
    @Mapping(source = "entity.group.id", target = "groupId")
    @Mapping(source = "entity.group.groupName", target = "groupName")
    @Mapping(source = "entity.creator.username", target = "creatorName")
    ShoppingListDTO mapToDto(ShoppingList entity);

    @Named("nrOfProducts")
    default int mapToNrOfProducts(ShoppingList shoppingList) {
        return shoppingList.getProductsList().size();
    }

    @Named("nrOfPurchasedProducts")
    default int mapTonrOfPurchasedProducts(ShoppingList shoppingList) {
        return (int) shoppingList.getProductsList()
                .stream()
                .filter(ShoppingListProduct::isPurchased)
                .count();
    }
}
