package com.cookie.app.controller;

import com.cookie.app.exception.*;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ProductDTO;
import com.cookie.app.model.enums.Category;
import com.cookie.app.model.enums.Unit;
import com.cookie.app.model.request.ReservePantryProductRequest;
import com.cookie.app.service.PantryProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PantryProductControllerTest {
    final long pantryProductId = 1L;
    final long pantryId = 1L;
    final int reserved = 1;

    @Mock
    private PantryProductService pantryProductService;
    @InjectMocks
    private PantryProductController pantryProductController;

    private Authentication authentication;

    @BeforeEach
    void init() {
        this.authentication = new UsernamePasswordAuthenticationToken("username", "password", Collections.emptyList());
    }

    @Test
    void test_getPantryProductsSuccess() {
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());
        PageImpl<PantryProductDTO> pageResponse = new PageImpl<>(pantryProductDTOS);

        doReturn(pageResponse).when(pantryProductService).getPantryProducts(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        ResponseEntity<Page<PantryProductDTO>> response = this.pantryProductController.getPantryProducts(this.pantryId, 0, "", "", "", authentication);

        assertEquals(pantryProductDTOS.size(), response.getBody().getTotalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_getPantryProductsPantryNotFound() {
        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryProductService).getPantryProducts(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryProductController.getPantryProducts(this.pantryId, 0, "", "", "", authentication));
    }

    @Test
    void test_addProductsToPantrySuccess() {
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doNothing().when(pantryProductService).addProductsToPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryProductController.addProductsToPantry(this.pantryId, pantryProductDTOS, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_addProductsToPantryPantryNotFound() {
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doThrow(new UserPerformedForbiddenActionException("Pantry not found"))
                .when(pantryProductService).addProductsToPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryProductController.addProductsToPantry(this.pantryId, pantryProductDTOS, authentication));
    }

    @Test
    void test_addProductsToPantryInvalidData() {
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(createPantryProduct());

        doThrow(new ValidationException("Invalid data"))
                .when(pantryProductService).addProductsToPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());

        assertThrows(ValidationException.class, () -> this.pantryProductController.addProductsToPantry(this.pantryId, pantryProductDTOS, authentication));
    }

    @Test
    void test_removeProductsFromPantrySuccess() {
        List<Long> pantryProductsIds = Collections.singletonList(this.pantryProductId);

        doNothing().when(pantryProductService).removeProductsFromPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryProductController.removeProductsFromPantry(this.pantryId, pantryProductsIds, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_removeProductsFromPantryModifyingDifferentPantry() {
        List<Long> pantryProductsIds = Collections.singletonList(this.pantryProductId);

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).removeProductsFromPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryProductController.removeProductsFromPantry(this.pantryId, pantryProductsIds, authentication));
    }

    @Test
    void test_modifyPantryProductSuccess() {
        PantryProductDTO pantryProductDTO = createPantryProduct();

        doNothing().when(pantryProductService).modifyPantryProduct(Mockito.anyLong(), Mockito.any(PantryProductDTO.class), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryProductController.modifyPantryProduct(this.pantryId, pantryProductDTO, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_modifyPantryProductModifyingDifferentPantry() {
        PantryProductDTO pantryProductDTO = createPantryProduct();

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).modifyPantryProduct(Mockito.anyLong(), Mockito.any(PantryProductDTO.class), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryProductController.modifyPantryProduct(this.pantryId, pantryProductDTO, authentication));
    }

    @Test
    void test_reservePantryProductSuccess() {
        ReservePantryProductRequest request = new ReservePantryProductRequest(this.reserved);
        PantryProductDTO pantryProductDTO = createPantryProduct();

        doReturn(pantryProductDTO).when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());
        ResponseEntity<PantryProductDTO> response = this.pantryProductController.reservePantryProduct(this.pantryId, pantryProductId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, pantryProductDTO.id());
    }

    @Test
    void test_reservePantryProductModifyingDifferentPantry() {
        ReservePantryProductRequest request = new ReservePantryProductRequest(this.reserved);

        doThrow(new UserPerformedForbiddenActionException("User tried to access wrong pantry"))
                .when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryProductController.reservePantryProduct(this.pantryId, pantryProductId, request, authentication));
    }

    @Test
    void test_reservePantryProductPantryProductNotFound() {
        ReservePantryProductRequest request = new ReservePantryProductRequest(this.reserved);

        doThrow(new UserPerformedForbiddenActionException("Pantry product not found"))
                .when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());

        assertThrows(UserPerformedForbiddenActionException.class, () -> this.pantryProductController.reservePantryProduct(this.pantryId, pantryProductId, request, authentication));
    }

    @Test
    void test_reservePantryProductReturnsNullBody() {
        ReservePantryProductRequest request = new ReservePantryProductRequest(this.reserved);

        doReturn(null).when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());
        ResponseEntity<PantryProductDTO> response = this.pantryProductController.reservePantryProduct(this.pantryId, pantryProductId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }

    private PantryProductDTO createPantryProduct() {
        ProductDTO productDTO = new ProductDTO(0L, "name", Category.CEREAL);

        return new PantryProductDTO(
                this.pantryProductId,
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
