package com.cookie.app.controller;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.service.PantryProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
public class PantryProductController {
    private static final String PANTRY_PRODUCTS_URL = "/pantry/{id}/products";
    private static final String GET_PANTRY_PRODUCTS_URL = "/pantry/{id}/products/{page}";
    private final PantryProductService pantryProductService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GET_PANTRY_PRODUCTS_URL)
    public ResponseEntity<Page<PantryProductDTO>> getPantryProducts(
            @PathVariable(value = "id") long id,
            @PathVariable(value = "page") int page,
            @RequestParam String filterValue,
            @RequestParam String sortColName,
            @RequestParam String sortDirection,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pantryProductService.getPantryProducts(
                        id, page, filterValue, sortColName, sortDirection, authentication.getName())
                );
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(PANTRY_PRODUCTS_URL)
    public ResponseEntity<Void> addProductsToPantry(
            @PathVariable(value = "id") long id,
            @NotEmpty(message = "List of products cannot be empty") @RequestBody List<@Valid PantryProductDTO> products,
            Authentication authentication
    ) {
        this.pantryProductService.addProductsToPantry(id, products, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(PANTRY_PRODUCTS_URL)
    public ResponseEntity<Void> removeProductsFromPantry(
            @PathVariable(value = "id") long id,
            @NotEmpty(message = "List of ids cannot be empty") @RequestBody List<Long> productIds,
            Authentication authentication
    ) {
        this.pantryProductService.removeProductsFromPantry(id, productIds, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(PANTRY_PRODUCTS_URL)
    public ResponseEntity<Void> modifyPantryProduct(
            @PathVariable(value = "id") long id,
            @Valid @RequestBody PantryProductDTO pantryProductDTO,
            Authentication authentication
    ) {
        this.pantryProductService.modifyPantryProduct(id, pantryProductDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
