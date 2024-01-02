package com.cookie.app.controller;

import com.cookie.app.exception.*;
import com.cookie.app.model.dto.PantryProductDTO;
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

@ExtendWith(MockitoExtension.class)
class PantryProductControllerTest {
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
        PantryProductDTO pantryProductDTO = new PantryProductDTO(1L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 0, null);
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(pantryProductDTO);
        PageImpl<PantryProductDTO> pageResponse = new PageImpl<>(pantryProductDTOS);

        Mockito.doReturn(pageResponse).when(pantryProductService).getPantryProducts(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
        ResponseEntity<Page<PantryProductDTO>> response = this.pantryProductController.getPantryProducts(1L, 0, "", "", "", authentication);

        assertEquals(pantryProductDTOS.size(), response.getBody().getTotalElements());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_getPantryProductsPantryNotFound() {
        Mockito.doThrow(new PantryNotFoundException("Pantry not found"))
                .when(pantryProductService).getPantryProducts(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString());

        assertThrows(PantryNotFoundException.class, () -> this.pantryProductController.getPantryProducts(1L, 0, "", "", "", authentication));
    }

    @Test
    void test_addProductsToPantrySuccess() {
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 0, null);
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(pantryProductDTO);

        Mockito.doNothing().when(pantryProductService).addProductsToPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryProductController.addProductsToPantry(1L, pantryProductDTOS, authentication);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_addProductsToPantryPantryNotFound() {
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 0, null);
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(pantryProductDTO);

        Mockito.doThrow(new PantryNotFoundException("Pantry not found"))
                .when(pantryProductService).addProductsToPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());

        assertThrows(PantryNotFoundException.class, () -> this.pantryProductController.addProductsToPantry(1L, pantryProductDTOS, authentication));
    }

    @Test
    void test_addProductsToPantryInvalidData() {
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 0, null);
        List<PantryProductDTO> pantryProductDTOS = Collections.singletonList(pantryProductDTO);

        Mockito.doThrow(new InvalidProductDataException("Invalid data"))
                .when(pantryProductService).addProductsToPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());

        assertThrows(InvalidProductDataException.class, () -> this.pantryProductController.addProductsToPantry(1L, pantryProductDTOS, authentication));
    }

    @Test
    void test_removeProductsFromPantrySuccess() {
        List<Long> pantryProductsIds = Collections.singletonList(1L);

        Mockito.doNothing().when(pantryProductService).removeProductsFromPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryProductController.removeProductsFromPantry(1L, pantryProductsIds, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_removeProductsFromPantryModifyingDifferentPantry() {
        List<Long> pantryProductsIds = Collections.singletonList(1L);

        Mockito.doThrow(new ModifyingProductsFromWrongPantryException("User tried to access wrong pantry"))
                .when(pantryProductService).removeProductsFromPantry(Mockito.anyLong(), Mockito.anyList(), Mockito.anyString());

        assertThrows(ModifyingProductsFromWrongPantryException.class, () -> this.pantryProductController.removeProductsFromPantry(1L, pantryProductsIds, authentication));
    }

    @Test
    void test_modifyPantryProductSuccess() {
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 0, null);

        Mockito.doNothing().when(pantryProductService).modifyPantryProduct(Mockito.anyLong(), Mockito.any(PantryProductDTO.class), Mockito.anyString());
        ResponseEntity<Void> response = this.pantryProductController.modifyPantryProduct(1L, pantryProductDTO, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_modifyPantryProductModifyingDifferentPantry() {
        PantryProductDTO pantryProductDTO = new PantryProductDTO(0L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 0, null);

        Mockito.doThrow(new ModifyingProductsFromWrongPantryException("User tried to access wrong pantry"))
                .when(pantryProductService).modifyPantryProduct(Mockito.anyLong(), Mockito.any(PantryProductDTO.class), Mockito.anyString());

        assertThrows(ModifyingProductsFromWrongPantryException.class, () -> this.pantryProductController.modifyPantryProduct(1L, pantryProductDTO, authentication));
    }

    @Test
    void test_reservePantryProductSuccess() {
        long pantryProductId = 1L;
        ReservePantryProductRequest request = new ReservePantryProductRequest(1);
        PantryProductDTO pantryProductDTO = new PantryProductDTO(1L, "name", Category.CEREAL, null, null, 1, Unit.GRAMS, 1, null);

        Mockito.doReturn(pantryProductDTO).when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());
        ResponseEntity<PantryProductDTO> response = this.pantryProductController.reservePantryProduct(1L, pantryProductId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, pantryProductDTO.id());
    }

    @Test
    void test_reservePantryProductModifyingDifferentPantry() {
        long pantryProductId = 1L;
        ReservePantryProductRequest request = new ReservePantryProductRequest(1);

        Mockito.doThrow(new ModifyingProductsFromWrongPantryException("User tried to access wrong pantry"))
                .when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());

        assertThrows(ModifyingProductsFromWrongPantryException.class, () -> this.pantryProductController.reservePantryProduct(1L, pantryProductId, request, authentication));
    }

    @Test
    void test_reservePantryProductPantryProductNotFound() {
        long pantryProductId = 1L;
        ReservePantryProductRequest request = new ReservePantryProductRequest(1);

        Mockito.doThrow(new PantryProductNotFoundException("Pantry product not found"))
                .when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());

        assertThrows(PantryProductNotFoundException.class, () -> this.pantryProductController.reservePantryProduct(1L, pantryProductId, request, authentication));
    }

    @Test
    void test_reservePantryProductReturnsNullBody() {
        long pantryProductId = 1L;
        ReservePantryProductRequest request = new ReservePantryProductRequest(1);

        Mockito.doReturn(null).when(pantryProductService).reservePantryProduct(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyInt(), Mockito.anyString());
        ResponseEntity<PantryProductDTO> response = this.pantryProductController.reservePantryProduct(1L, pantryProductId, request, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
    }
}
