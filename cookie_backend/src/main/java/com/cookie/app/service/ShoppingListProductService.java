package com.cookie.app.service;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ShoppingListProductService {
    Page<ShoppingListProductDTO> getShoppingListProducts(
            long groupId,
            int page,
            String filterValue,
            String sortColName,
            String sortDirection,
            String userEmail
    );
    void addProductsToShoppingList(long groupId, List<ShoppingListProductDTO> productDTOList, String userEmail);
    void removeProductsFromShoppingList(long groupId, List<Long> productIds, String userEmail);
    void modifyShoppingListProduct(long groupId, ShoppingListProductDTO productDTO, String userEmail);
    void changePurchaseStatusForProducts(long groupId, List<Long> productIds, String userEmail);
    void transferProductsToPantry(long shoppingListId, String userEmail);
}
