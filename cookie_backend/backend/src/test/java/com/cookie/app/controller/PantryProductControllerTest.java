package com.cookie.app.controller;

import com.cookie.app.exception.*;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.FilterRequest;
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
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryProductControllerTest extends AbstractControllerTest {
    private final long pantryProductId = 1L;
    private final long pantryId = 1L;
    private final int reserved = 1;

    @Mock
    private PantryProductService pantryProductService;
    @InjectMocks
    private PantryProductController controller;

    @Test
    void test_getPantryProductsSuccessful() {
        final FilterRequest filterRequest = new FilterRequest("", "", null);
        final int pageNr = 1;
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());
        final PageResult<PantryProductDTO> pageResponse = new PageResult<>(pantryProductDTOS, pantryProductDTOS.size(), 1, 0);

        doReturn(pageResponse).when(pantryProductService)
                .getPantryProducts(pantryId, pageNr, filterRequest, authentication.getName());
        ResponseEntity<PageResult<PantryProductDTO>> response =
                controller.getPantryProducts(pantryId, pageNr, filterRequest, authentication);

        assertThat(Objects.requireNonNull(response.getBody()).totalElements()).isEqualTo(pantryProductDTOS.size());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_getPantryProductsPantryNotFound() {
        final FilterRequest filterRequest = new FilterRequest("", "", null);
        final int pageNr = 1;

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryProductService).getPantryProducts(pantryId, pageNr, filterRequest, authentication.getName());

        assertThatThrownBy(() -> controller.getPantryProducts(pantryId, 1, filterRequest, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Pantry not found");
    }

    @Test
    void test_addProductsToPantrySuccessful() {
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        ResponseEntity<Void> response = controller.addProductsToPantry(pantryId, pantryProductDTOS, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void test_addProductsToPantryPantryNotFound() {
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryProductService).addProductsToPantry(pantryId, pantryProductDTOS, authentication.getName());

        assertThatThrownBy(() -> controller.addProductsToPantry(pantryId, pantryProductDTOS, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Pantry not found");
    }

    @Test
    void test_addProductsToPantryInvalidData() {
        final List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doThrow(new ValidationException("Invalid data"))
                .when(pantryProductService).addProductsToPantry(pantryId, pantryProductDTOS, authentication.getName());

        assertThatThrownBy(() -> controller.addProductsToPantry(pantryId, pantryProductDTOS, authentication))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Invalid data");
    }

    @Test
    void test_removeProductsFromPantrySuccessful() {
        final List<Long> pantryProductsIds = Collections.singletonList(pantryProductId);

        ResponseEntity<Void> response = controller.removeProductsFromPantry(pantryId, pantryProductsIds, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_removeProductsFromPantryModifyingDifferentPantry() {
        final List<Long> pantryProductsIds = Collections.singletonList(pantryProductId);

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).removeProductsFromPantry(pantryId, pantryProductsIds, authentication.getName());

        assertThatThrownBy(() -> controller.removeProductsFromPantry(pantryId, pantryProductsIds, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("User tried to access wrong pantry");
    }

    @Test
    void test_updatePantryProductSuccessful() {
        final PantryProductDTO pantryProductDTO = createPantryProduct();

        ResponseEntity<Void> response = controller.updatePantryProduct(pantryId, pantryProductDTO, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void test_updatePantryProductUpdatingDifferentPantry() {
        final PantryProductDTO pantryProductDTO = createPantryProduct();

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).updatePantryProduct(pantryId, pantryProductDTO, authentication.getName());

        assertThatThrownBy(() -> controller.updatePantryProduct(pantryId, pantryProductDTO, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("User tried to access wrong pantry");
    }

    @Test
    void test_reservePantryProductSuccessful() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);
        final PantryProductDTO pantryProductDTO = createPantryProduct();

        doReturn(pantryProductDTO).when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());
        ResponseEntity<PantryProductDTO> response = controller.reservePantryProduct(pantryId, pantryProductId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).id()).isEqualTo(1L);
    }

    @Test
    void test_reservePantryProductModifyingDifferentPantry() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());

        assertThatThrownBy(() -> controller.reservePantryProduct(pantryId, pantryProductId, request, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("User tried to access wrong pantry");
    }

    @Test
    void test_reservePantryProductPantryProductNotFound() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);

        doThrow(new UserPerformedForbiddenActionException("Pantry product not found"))
                .when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());

        assertThatThrownBy(() -> controller.reservePantryProduct(pantryId, pantryProductId, request, authentication))
                .isInstanceOf(UserPerformedForbiddenActionException.class)
                .hasMessage("Pantry product not found");
    }

    @Test
    void test_reservePantryProductReturnsNullBody() {
        final ReservePantryProductRequest request = new ReservePantryProductRequest(reserved);

        doReturn(null).when(pantryProductService).reservePantryProduct(pantryId, pantryProductId, request.reserved(), authentication.getName());
        ResponseEntity<PantryProductDTO> response = controller.reservePantryProduct(pantryId, pantryProductId, request, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
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