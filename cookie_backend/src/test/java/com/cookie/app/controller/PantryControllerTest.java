package com.cookie.app.controller;

import com.cookie.app.exception.UserPerformedForbiddenActionException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.dto.PantryDTO;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.service.PantryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PantryControllerTest {
    final String username = "username";
    final String pantryName = "pantryName";
    final String groupName = "groupName";
    final String password = "password";
    final Long id = 1L;

    @Mock
    private PantryService pantryService;
    @InjectMocks
    private PantryController pantryController;
    private Authentication authentication;

    @BeforeEach
    void init() {
        this.authentication = new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
    }

    @Test
    void test_createPantrySuccess() {
        CreatePantryRequest request = new CreatePantryRequest(pantryName, id);
        GetPantryResponse getPantryResponse = new GetPantryResponse(id, pantryName, id, groupName, Collections.emptySet());

        Mockito.doReturn(getPantryResponse).when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.createPantry(request, this.authentication);

        assertSame(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(getPantryResponse.pantryId(), response.getBody().pantryId());
        assertEquals(getPantryResponse.pantryName(), response.getBody().pantryName());
        assertTrue(response.getBody().authorities().isEmpty());
    }

    @Test
    void test_createPantryUserNotFoundAfterAuth() {
        CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.createPantry(request, this.authentication));
    }

    @Test
    void test_createPantryForNonExistingGroup() {
        CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        Mockito.doThrow(new UserPerformedForbiddenActionException("You tried to create pantry for non existing group"))
                .when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryController.createPantry(request, this.authentication));
    }

    @Test
    void test_createPantryWithoutPermission() {
        CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        Mockito.doThrow(new UserPerformedForbiddenActionException("You tried to create pantry for non existing group"))
                .when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryController.createPantry(request, this.authentication));
    }

    @Test
    void test_getUserPantrySuccess() {
        GetPantryResponse getPantryResponse = new GetPantryResponse(id, pantryName, id, groupName, Collections.emptySet());

        Mockito.doReturn(getPantryResponse).when(pantryService).getPantry(Mockito.anyLong(), Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.getPantry(id, this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(getPantryResponse.pantryId(), response.getBody().pantryId());
        assertEquals(getPantryResponse.pantryName(), response.getBody().pantryName());
        assertTrue(response.getBody().authorities().isEmpty());
    }

    @Test
    void test_getUserPantryReturnsNullPantry() {
        GetPantryResponse getPantryResponse = new GetPantryResponse(0, null, 0, null,null);

        Mockito.doReturn(getPantryResponse).when(pantryService).getPantry(Mockito.anyLong(), Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.getPantry(id, this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().pantryId());
        assertNull(response.getBody().pantryName());
        assertNull(response.getBody().authorities());
    }

    @Test
    void test_getUserPantryUserNotFound() {

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).getPantry(Mockito.anyLong(), Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.getPantry(id, this.authentication));
    }

    @Test
    void test_getAllUserPantriesSuccess() {
        PantryDTO pantryDTO = new PantryDTO(id, pantryName, 3, 3L, "groupName");
        GetUserPantriesResponse getUserPantriesResponse = new GetUserPantriesResponse(Collections.singletonList(pantryDTO));

        Mockito.doReturn(getUserPantriesResponse).when(pantryService).getAllUserPantries(Mockito.anyString());
        ResponseEntity<GetUserPantriesResponse> receivedResponse = this.pantryController.getAllUserPantries(this.authentication);

        assertSame(HttpStatus.OK, receivedResponse.getStatusCode());
        assertEquals(1, receivedResponse.getBody().pantries().size());
        assertEquals(pantryDTO.pantryId(), receivedResponse.getBody().pantries().get(0).pantryId());
        assertEquals(pantryDTO.pantryName(), receivedResponse.getBody().pantries().get(0).pantryName());
        assertEquals(pantryDTO.nrOfProducts(), receivedResponse.getBody().pantries().get(0).nrOfProducts());
        assertEquals(pantryDTO.groupId(), receivedResponse.getBody().pantries().get(0).groupId());
        assertEquals(pantryDTO.groupName(), receivedResponse.getBody().pantries().get(0).groupName());
    }

    @Test
    void test_getAllUserPantriesUserNotFound() {

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).getAllUserPantries(Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.getAllUserPantries(this.authentication));
    }

    @Test
    void test_deleteUserPantrySuccess() {
        DeletePantryResponse deletePantryResponse = new DeletePantryResponse(pantryName);

        Mockito.doReturn(deletePantryResponse).when(pantryService).deletePantry(Mockito.anyLong(), Mockito.anyString());
        ResponseEntity<DeletePantryResponse> response = this.pantryController.deletePantry(id, this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(deletePantryResponse.deletedPantryName(), response.getBody().deletedPantryName());
    }

    @Test
    void test_deleteUserPantryPantryNotFound() {

        Mockito.doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryService).deletePantry(Mockito.anyLong() ,Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryController.deletePantry(id, this.authentication));
    }

    @Test
    void test_deleteUserPantryUserNotFound() {

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).deletePantry(Mockito.anyLong(), Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.deletePantry(id, this.authentication));
    }

    @Test
    void test_updateUserPantrySuccess() {
        UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");
        GetPantryResponse updatePantryResponse = new GetPantryResponse(id, pantryName, id, groupName, Collections.emptySet());

        Mockito.doReturn(updatePantryResponse).when(pantryService).updatePantry(Mockito.anyLong(), Mockito.any(UpdatePantryRequest.class), Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.updatePantry(id, updatePantryRequest, this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatePantryResponse.pantryId(), response.getBody().pantryId());
        assertEquals(updatePantryResponse.pantryName(), response.getBody().pantryName());
    }

    @Test
    void test_updateUserPantryPantryNotFound() {
        UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");

        Mockito.doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryService).updatePantry(Mockito.anyLong(), Mockito.any(UpdatePantryRequest.class), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryController.updatePantry(id, updatePantryRequest, this.authentication));
    }

    @Test
    void test_updateUserPantryUserNotFound() {
        UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).updatePantry(Mockito.anyLong(), Mockito.any(UpdatePantryRequest.class), Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.updatePantry(id, updatePantryRequest, this.authentication));
    }
}
