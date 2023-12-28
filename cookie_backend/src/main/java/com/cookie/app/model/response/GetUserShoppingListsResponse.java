package com.cookie.app.model.response;

import com.cookie.app.model.dto.ShoppingListDTO;

import java.util.List;

public record GetUserShoppingListsResponse(List<ShoppingListDTO> shoppingLists) {}
