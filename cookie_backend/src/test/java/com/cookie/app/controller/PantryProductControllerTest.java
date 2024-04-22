package com.cookie.app.controller;

import com.cookie.app.exception.*;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.ReservePantryProductRequest;
import com.cookie.app.service.PantryProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryProductControllerTest extends AbstractControllerTest {
    final long pantryProductId = 1L;
    final long pantryId = 1L;
    final int reserved = 1;

    @Mock
    private PantryProductService pantryProductService;
    @InjectMocks
    private PantryProductController controller;

    @Test
    void test_getPantryProductsSuccessful() {
        final int pageNr = 1;
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());
        final PageResult<PantryProductDTO> pageResponse = new PageResult<>(pantryProductDTOS, pantryProductDTOS.size(), 1, 0);

        doReturn(pageResponse).when(pantryProductService)
                .getPantryProducts(this.pantryId, pageNr, "", "", "", authentication.getName());
        ResponseEntity<PageResult<PantryProductDTO>> response =
                this.controller.getPantryProducts(pantryId, pageNr, "", "", "", authentication);

        assertEquals(pantryProductDTOS.size(), response.getBody().totalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_getPantryProductsPantryNotFound() {
        final int pageNr = 1;

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryProductService).getPantryProducts(pantryId, pageNr, "", "", "", authentication.getName());

        assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.controller.getPantryProducts(pantryId, 1, "", "", "", authentication));
    }

    @Test
    void test_addProductsToPantrySuccessful() {
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        ResponseEntity<Void> response = this.controller.addProductsToPantry(pantryId, pantryProductDTOS, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_addProductsToPantryPantryNotFound() {
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryProductService).addProductsToPantry(pantryId, pantryProductDTOS, authentication.getName());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.controller.addProductsToPantry(pantryId, pantryProductDTOS, authentication));
    }

    @Test
    void test_addProductsToPantryInvalidData() {
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doThrow(new ValidationException("Invalid data"))
                .when(pantryProductService).addProductsToPantry(pantryId, pantryProductDTOS, authentication.getName());

        assertThrows(ValidationException.class, () -> this.controller.addProductsToPantry(pantryId, pantryProductDTOS, authentication));
    }

    @Test
    void test_removeProductsFromPantrySuccessful() {
        final List<Long> pantryProductsIds = Collections.singletonList(pantryProductId);

        ResponseEntity<Void> response = this.controller.removeProductsFromPantry(pantryId, pantryProductsIds, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_removeProductsFromPantryModifyingDifferentPantry() {
        final List<Long> pantryProductsIds = Collections.singletonList(pantryProductId);

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).removeProductsFromPantry(pantryId, pantryProductsIds, authentication.getName());

        assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.controller.removeProductsFromPantry(pantryId, pantryProductsIds, authentication));
    }

    @Test
    void test_updatePantryProductSuccessful() {
        final PantryProductDTO pantryProductDTO = createPantryProduct();

        ResponseEntity<Void> response = this.controller.updatePantryProduct(pantryId, pantryProductDTO, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_updatePantryProductUpdatingDifferentPantry() {
        final PantryProductDTO pantryProductDTO = createPantryProduct();

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).updatePantryProduct(pantryId, pantryProductDTO, authentication.getName());

        assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.controller.updatePantryProduct(pantryId, pantryProductDTO, authentication));
    }

    @Test
    void test_reservePantryProductSuccessful() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);
        final PantryProductDTO pantryProductDTO = createPantryProduct();

        doReturn(pantryProductDTO).when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());
        ResponseEntity<PantryProductDTO> response = this.controller.reservePantryProduct(pantryId, pantryProductId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, pantryProductDTO.id());
    }

    @Test
    void test_reservePantryProductModifyingDifferentPantry() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());

        assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.controller.reservePantryProduct(pantryId, pantryProductId, request, authentication));
    }

    @Test
    void test_reservePantryProductPantryProductNotFound() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(this.reserved);

        doThrow(new UserPerformedForbiddenActionException("Pantry product not found"))
                .when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());

        assertThrows(UserPerformedForbiddenActionException.class, () ->
                this.controller.reservePantryProduct(pantryId, pantryProductId, request, authentication));
    }

    @Test
    void test_reservePantryProductReturnsNullBody() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);

        doReturn(null).when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());
        ResponseEntity<PantryProductDTO> response = this.controller.reservePantryProduct(pantryId, pantryProductId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    private PantryProductDTO createPantryProduct() {
        final ProductDTO productDTO = new ProductDTO(0L, "name", Category.CEREAL);

        return new PantryProductDTO(
                pantryProductId,
                productDTO,
                null,
                null,
                1,
                Unit.GRAMS,
                0,
                null
        );
    }
}
