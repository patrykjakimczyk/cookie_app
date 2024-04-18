package com.cookie.app.controller;

import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ShoppingListControllerTest extends AbstractControllerTest{
    final String listName = "listName";
    @Mock
    private ShoppingListService shoppingListService;
    @InjectMocks
    private ShoppingListController controller;

    @Test
    void test_createShoppingListSuccessful() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, 1L);
        final GetShoppingListResponse serviceResponse = new GetShoppingListResponse(1L, listName, Collections.emptySet(), false);

        doReturn(serviceResponse).when(shoppingListService).createShoppingList(request, super.username);
        ResponseEntity<GetShoppingListResponse> response = this.controller.createShoppingList(request, super.authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(serviceResponse.listName(), response.getBody().listName());
    }
}
