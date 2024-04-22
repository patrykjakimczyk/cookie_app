package com.cookie.app.service;

import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;

import java.util.List;

public interface ShoppingListProductService {
    PageResult<ShoppingListProductDTO> getShoppingListProducts(
            long listId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );
    void addProductsToShoppingList(long listId, List<ShoppingListProductDTO> listProductDTOList, String userEmail);
    void removeProductsFromShoppingList(long listId, List<Long> listProductIds, String userEmail);
    void updateShoppingListProduct(long listId, ShoppingListProductDTO listProductDTO, String userEmail);
    void changePurchaseStatusForProducts(long listId, List<Long> listProductIds, String userEmail);
    void transferProductsToPantry(long listId, String userEmail);
    void addRecipeProductsToShoppingList(long listId, User user, List<RecipeProduct> recipeProducts);
}
