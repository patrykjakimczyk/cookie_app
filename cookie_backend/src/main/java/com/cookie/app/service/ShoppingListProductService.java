package com.cookie.app.service;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.entity.RecipeProduct;
import com.cookie.app.model.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ShoppingListProductService {
    Page<ShoppingListProductDTO> getShoppingListProducts(
            long listId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );
    void addProductsToShoppingList(long listId, List<ShoppingListProductDTO> listProductDTOList, String userEmail);
    void removeProductsFromShoppingList(long listId, List<Long> listProductIds, String userEmail);
    void modifyShoppingListProduct(long listId, ShoppingListProductDTO listProductDTO, String userEmail);
    void changePurchaseStatusForProducts(long listId, List<Long> listProductIds, String userEmail);
    void transferProductsToPantry(long listId, String userEmail);
    List<ShoppingListProductDTO> addRecipeProductsToShoppingList(long listId, User user, List<RecipeProduct> recipeProducts);
}
