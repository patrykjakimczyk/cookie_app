package com.cookie.app.controller;

import com.cookie.app.model.dto.ShoppingListDTO;
import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;
import com.cookie.app.service.ShoppingListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class ShoppingListControllerTest extends AbstractControllerTest {
    final String listName = "listName";
    final long id = 1L;
    @Mock
    private ShoppingListService shoppingListService;
    @InjectMocks
    private ShoppingListController controller;

    @Test
    void test_createShoppingListSuccessful() {
        final CreateShoppingListRequest request = new CreateShoppingListRequest(listName, 1L);
        final GetShoppingListResponse serviceResponse = new GetShoppingListResponse(id, listName, Collections.emptySet(), false);

        doReturn(serviceResponse).when(shoppingListService).createShoppingList(request, username);
        ResponseEntity<GetShoppingListResponse> response = this.controller.createShoppingList(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().listName()).isEqualTo(serviceResponse.listName());
    }

    @Test
    void test_getShoppingListSuccessful() {
        final GetShoppingListResponse serviceResponse = new GetShoppingListResponse(id, listName, Collections.emptySet(), false);

        doReturn(serviceResponse).when(shoppingListService).getShoppingList(id, username);
        ResponseEntity<GetShoppingListResponse> response = this.controller.getShoppingList(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().listName()).isEqualTo(serviceResponse.listName());
    }

    @Test
    void test_getShoppingListSuccessfulPantryNotFound() {
        final GetShoppingListResponse serviceResponse = new GetShoppingListResponse(0L, null, Collections.emptySet(), false);

        doReturn(serviceResponse).when(shoppingListService).getShoppingList(id, username);
        ResponseEntity<GetShoppingListResponse> response = this.controller.getShoppingList(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().listName()).isEqualTo(serviceResponse.listName());
    }

    @Test
    void test_getAllUserShoppingListsSuccessful() {
        ShoppingListDTO shoppingListDTO = new ShoppingListDTO(id, listName, 1, 1, id, "groupName", "creator");
        final GetUserShoppingListsResponse serviceResponse = new GetUserShoppingListsResponse(List.of(shoppingListDTO));

        doReturn(serviceResponse).when(shoppingListService).getUserShoppingLists(username);
        ResponseEntity<GetUserShoppingListsResponse> response = this.controller.getAllUserShoppingLists(authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().shoppingLists()).hasSize(1);
        assertThat(response.getBody().shoppingLists().get(0)).isEqualTo(shoppingListDTO);
    }

    @Test
    void test_deleteShoppingListSuccessful() {
        final DeleteShoppingListResponse serviceResponse = new DeleteShoppingListResponse(listName);

        doReturn(serviceResponse).when(shoppingListService).deleteShoppingList(id, username);
        ResponseEntity<DeleteShoppingListResponse> response = this.controller.deleteShoppingList(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().deletedListName()).isEqualTo(serviceResponse.deletedListName());
    }

    @Test
    void test_updateShoppingListSuccessful() {
        final UpdateShoppingListRequest updateRequest = new UpdateShoppingListRequest("newListName");
        final GetShoppingListResponse serviceResponse = new GetShoppingListResponse(id, "newListName", Collections.emptySet(), false);

        doReturn(serviceResponse).when(shoppingListService).updateShoppingList(id, updateRequest, username);
        ResponseEntity<GetShoppingListResponse> response = this.controller.updateShoppingList(id, updateRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().listName()).isEqualTo(serviceResponse.listName());
    }
}