package com.cookie.app.controller;

import com.cookie.app.exception.PantryNotFoundException;
import com.cookie.app.exception.UserHasAssignedPantryException;
import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
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
    @Mock
    private PantryService pantryService;

    @InjectMocks
    private PantryController pantryController;

    private Authentication authentication;

    @BeforeEach
    void init() {
        this.authentication = new UsernamePasswordAuthenticationToken("username", "password", Collections.emptyList());
    }

    @Test
    void test_createUserPantrySuccess() {
        CreatePantryRequest request = new CreatePantryRequest("pantry");

        Mockito.doNothing().when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryController.createPantry(request, this.authentication);

        assertSame(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_createUserPantryUserNotFoundAfterAuth() {
        CreatePantryRequest request = new CreatePantryRequest("pantry");

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.createPantry(request, this.authentication));
    }

    @Test
    void test_createUserPantryUserHasPantry() {
        CreatePantryRequest request = new CreatePantryRequest("pantry");

        Mockito.doThrow(new UserHasAssignedPantryException("User has pantry"))
                .when(pantryService).createPantry(Mockito.any(CreatePantryRequest.class), Mockito.anyString());

        assertThrows(UserHasAssignedPantryException.class, () -> this.pantryController.createPantry(request, this.authentication));
    }

    @Test
    void test_getUserPantrySuccess() {
        GetPantryResponse getPantryResponse = new GetPantryResponse(1L, "pantry");

        Mockito.doReturn(getPantryResponse).when(pantryService).getPantry(Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.getPantry(this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(getPantryResponse.id(), response.getBody().id());
        assertEquals(getPantryResponse.pantryName(), response.getBody().pantryName());
    }

    @Test
    void test_getUserPantryReturnsNullPantry() {
        GetPantryResponse getPantryResponse = new GetPantryResponse(null, null);

        Mockito.doReturn(getPantryResponse).when(pantryService).getPantry(Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.getPantry(this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(getPantryResponse.id(), response.getBody().id());
        assertEquals(getPantryResponse.pantryName(), response.getBody().pantryName());
    }

    @Test
    void test_getUserPantryUserNotFound() {

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).getPantry(Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.getPantry(this.authentication));
    }

    @Test
    void test_deleteUserPantrySuccess() {
        DeletePantryResponse deletePantryResponse = new DeletePantryResponse( "pantry");

        Mockito.doReturn(deletePantryResponse).when(pantryService).deletePantry(Mockito.anyString());
        ResponseEntity<DeletePantryResponse> response = this.pantryController.deletePantry(this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(deletePantryResponse.deletedPantryName(), response.getBody().deletedPantryName());
    }

    @Test
    void test_deleteUserPantryPantryNotFound() {

        Mockito.doThrow(new PantryNotFoundException("Pantry not found"))
                .when(pantryService).deletePantry(Mockito.anyString());

        assertThrows(PantryNotFoundException.class, () -> this.pantryController.deletePantry(this.authentication));
    }

    @Test
    void test_deleteUserPantryUserNotFound() {

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).deletePantry(Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.deletePantry(this.authentication));
    }

    @Test
    void test_updateUserPantrySuccess() {
        UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");
        GetPantryResponse updatePantryResponse = new GetPantryResponse(1L, "pantry");

        Mockito.doReturn(updatePantryResponse).when(pantryService).updatePantry(Mockito.any(UpdatePantryRequest.class), Mockito.anyString());
        ResponseEntity<GetPantryResponse> response = this.pantryController.updatePantry(updatePantryRequest, this.authentication);

        assertSame(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatePantryResponse.id(), response.getBody().id());
        assertEquals(updatePantryResponse.pantryName(), response.getBody().pantryName());
    }

    @Test
    void test_updateUserPantryPantryNotFound() {
        UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");

        Mockito.doThrow(new PantryNotFoundException("Pantry not found"))
                .when(pantryService).updatePantry(Mockito.any(UpdatePantryRequest.class), Mockito.anyString());

        assertThrows(PantryNotFoundException.class, () -> this.pantryController.updatePantry(updatePantryRequest, this.authentication));
    }

    @Test
    void test_updateUserPantryUserNotFound() {
        UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");

        Mockito.doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).updatePantry(Mockito.any(UpdatePantryRequest.class), Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.pantryController.updatePantry(updatePantryRequest, this.authentication));
    }
}
