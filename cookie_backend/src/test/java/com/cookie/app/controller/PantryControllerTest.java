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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class PantryControllerTest extends AbstractControllerTest {
    final String pantryName = "pantryName";
    final String groupName = "groupName";
    final Long id = 1L;

    @Mock
    private PantryService pantryService;
    @InjectMocks
    private PantryController controller;

    @Test
    void test_createPantrySuccess() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);
        final GetPantryResponse getPantryResponse = new GetPantryResponse(id, pantryName, id, groupName, Collections.emptySet());

        doReturn(getPantryResponse).when(pantryService).createPantry(request, authentication.getName());
        ResponseEntity<GetPantryResponse> response = this.controller.createPantry(request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pantryId()).isEqualTo(getPantryResponse.pantryId());
        assertThat(response.getBody().pantryName()).isEqualTo(getPantryResponse.pantryName());
        assertThat(response.getBody().authorities()).isEmpty();
    }

    @Test
    void test_createPantryUserNotFoundAfterAuth() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).createPantry(request, authentication.getName());

        assertThatThrownBy(() -> this.controller.createPantry(request, authentication))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class)
                .hasMessage("User not found");
    }

    @Test
    void test_createPantryForNonExistingGroup() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doThrow(new UserPerformedForbiddenActionException("You tried to create pantry for non existing group"))
                .when(pantryService).createPantry(request, authentication.getName());

        assertThatThrownBy(() -> this.controller.createPantry(request, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You tried to create pantry for non existing group");
    }

    @Test
    void test_createPantryWithoutPermission() {
        final CreatePantryRequest request = new CreatePantryRequest(pantryName, id);

        doThrow(new UserPerformedForbiddenActionException("You tried to create pantry for non existing group"))
                .when(pantryService).createPantry(request, authentication.getName());

        assertThatThrownBy(() -> this.controller.createPantry(request, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("You tried to create pantry for non existing group");
    }

    @Test
    void test_getUserPantrySuccess() {
        final GetPantryResponse getPantryResponse = new GetPantryResponse(id, pantryName, id, groupName, Collections.emptySet());

        doReturn(getPantryResponse).when(pantryService).getPantry(id, authentication.getName());
        ResponseEntity<GetPantryResponse> response = this.controller.getPantry(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pantryId()).isEqualTo(getPantryResponse.pantryId());
        assertThat(response.getBody().pantryName()).isEqualTo(getPantryResponse.pantryName());
        assertThat(response.getBody().authorities()).isEmpty();
    }

    @Test
    void test_getUserPantryReturnsNullPantry() {
        final GetPantryResponse getPantryResponse = new GetPantryResponse(0, null, 0, null, null);

        doReturn(getPantryResponse).when(pantryService).getPantry(id, authentication.getName());
        ResponseEntity<GetPantryResponse> response = this.controller.getPantry(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pantryId()).isZero();
        assertThat(response.getBody().pantryName()).isNull();
        assertThat(response.getBody().authorities()).isNull();
    }

    @Test
    void test_getUserPantryUserNotFound() {

        doThrow(new UserWasNotFoundAfterAuthException("User not found")).when(pantryService).getPantry(id, authentication.getName());

        assertThatThrownBy(() -> this.controller.getPantry(id, authentication))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class)
                .hasMessage("User not found");
    }

    @Test
    void test_getAllUserPantriesSuccess() {
        final PantryDTO pantryDTO = new PantryDTO(id, pantryName, 3, 3L, "groupName");
        final GetUserPantriesResponse getUserPantriesResponse = new GetUserPantriesResponse(Collections.singletonList(pantryDTO));

        doReturn(getUserPantriesResponse).when(pantryService).getAllUserPantries(authentication.getName());
        ResponseEntity<GetUserPantriesResponse> receivedResponse = this.controller.getAllUserPantries(authentication);

        assertThat(receivedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(receivedResponse.getBody()).isNotNull();
        assertThat(receivedResponse.getBody().pantries()).hasSize(1);
        assertThat(receivedResponse.getBody().pantries().get(0).pantryId()).isEqualTo(pantryDTO.pantryId());
        assertThat(receivedResponse.getBody().pantries().get(0).pantryName()).isEqualTo(pantryDTO.pantryName());
        assertThat(receivedResponse.getBody().pantries().get(0).nrOfProducts()).isEqualTo(pantryDTO.nrOfProducts());
        assertThat(receivedResponse.getBody().pantries().get(0).groupId()).isEqualTo(pantryDTO.groupId());
        assertThat(receivedResponse.getBody().pantries().get(0).groupName()).isEqualTo(pantryDTO.groupName());
    }

    @Test
    void test_getAllUserPantriesUserNotFound() {

        doThrow(new UserWasNotFoundAfterAuthException("User not found")).when(pantryService).getAllUserPantries(authentication.getName());

        assertThatThrownBy(() -> this.controller.getAllUserPantries(authentication))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class)
                .hasMessage("User not found");
    }

    @Test
    void test_deleteUserPantrySuccess() {
        final DeletePantryResponse deletePantryResponse = new DeletePantryResponse(pantryName);

        doReturn(deletePantryResponse).when(pantryService).deletePantry(id, authentication.getName());
        ResponseEntity<DeletePantryResponse> response = this.controller.deletePantry(id, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().deletedPantryName()).isEqualTo(deletePantryResponse.deletedPantryName());
    }

    @Test
    void test_deleteUserPantryPantryNotFound() {

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryService).deletePantry(id, authentication.getName());

        assertThatThrownBy(() -> this.controller.deletePantry(id, this.authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Pantry not found");
    }

    @Test
    void test_deleteUserPantryUserNotFound() {

        doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).deletePantry(id, authentication.getName());

        assertThatThrownBy(() -> this.controller.deletePantry(id, this.authentication))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class)
                .hasMessage("User not found");
    }

    @Test
    void test_updateUserPantrySuccess() {
        final UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");
        final GetPantryResponse updatePantryResponse = new GetPantryResponse(id, pantryName, id, groupName, Collections.emptySet());

        doReturn(updatePantryResponse).when(pantryService).updatePantry(id, updatePantryRequest, authentication.getName());
        ResponseEntity<GetPantryResponse> response = this.controller.updatePantry(id, updatePantryRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().pantryId()).isEqualTo(updatePantryResponse.pantryId());
        assertThat(response.getBody().pantryName()).isEqualTo(updatePantryResponse.pantryName());
    }

    @Test
    void test_updateUserPantryPantryNotFound() {
        final UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryService).updatePantry(id, updatePantryRequest, authentication.getName());

        assertThatThrownBy(() -> this.controller.updatePantry(id, updatePantryRequest, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Pantry not found");
    }

    @Test
    void test_updateUserPantryUserNotFound() {
        final UpdatePantryRequest updatePantryRequest = new UpdatePantryRequest("newName");

        doThrow(new UserWasNotFoundAfterAuthException("User not found"))
                .when(pantryService).updatePantry(id, updatePantryRequest, authentication.getName());

        assertThatThrownBy(() -> this.controller.updatePantry(id, updatePantryRequest, authentication))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class)
                .hasMessage("User not found");
    }
}