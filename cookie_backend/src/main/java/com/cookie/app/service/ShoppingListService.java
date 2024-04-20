package com.cookie.app.service;

import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;

public interface ShoppingListService {
    GetShoppingListResponse createShoppingList(CreateShoppingListRequest request, String userEmail);
    GetShoppingListResponse getShoppingList(long shoppingListId, String userEmail);
    GetUserShoppingListsResponse getUserShoppingLists(String userEmail);
    DeleteShoppingListResponse deleteShoppingList(long shoppingListId, String userEmail);
    GetShoppingListResponse updateShoppingList(long shoppingListId, UpdateShoppingListRequest request, String userEmail);

}
