package com.cookie.app.controller;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.request.ReservePantryProductRequest;
import com.cookie.app.service.PantryProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/v1/pantry/{pantryId}/products")
@RestController
public class PantryProductController {
    private static final String GET_PANTRY_PRODUCTS_URL = "/{page}";
    private static final String PANTRY_PRODUCT_URL = "/{productId}";
    private final PantryProductService pantryProductService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GET_PANTRY_PRODUCTS_URL)
    public ResponseEntity<Page<PantryProductDTO>> getPantryProducts(
            @PathVariable(value = "pantryId") @Valid @Min(value = 1, message = "Pantry id must be greater than 0") long pantryId,
            @PathVariable(value = "page") @Valid @Min(value = 0, message = "Page nr must be at least 0") int page,
            @RequestParam String filterValue,
            @RequestParam String sortColName,
            @RequestParam String sortDirection,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pantryProductService.getPantryProducts(
                        pantryId, page, filterValue, sortColName, sortDirection, authentication.getName())
                );
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<Void> addProductsToPantry(
            @PathVariable(value = "pantryId") @Valid @Min(value = 1, message = "Pantry id must be greater than 0") long pantryId,
            @Valid @NotEmpty(message = "List of products cannot be empty") @RequestBody List<@Valid PantryProductDTO> products,
            Authentication authentication
    ) {
        this.pantryProductService.addProductsToPantry(pantryId, products, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> removeProductsFromPantry(
            @PathVariable(value = "pantryId") @Valid @Min(value = 1,message = "Pantry id must be greater than 0") long pantryId,
            @Valid @NotEmpty(message = "List of ids cannot be empty") @RequestBody List<Long> productIds,
            Authentication authentication
    ) {
        this.pantryProductService.removeProductsFromPantry(pantryId, productIds, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping
    public ResponseEntity<Void> modifyPantryProduct(
            @PathVariable(value = "pantryId") @Valid @Min(value = 1, message = "Pantry id must be greater than 0") long pantryId,
            @Valid @RequestBody PantryProductDTO pantryProductDTO,
            Authentication authentication
    ) {
        this.pantryProductService.modifyPantryProduct(pantryId, pantryProductDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(PANTRY_PRODUCT_URL)
    public ResponseEntity<PantryProductDTO> reservePantryProduct(
            @PathVariable(value = "pantryId") @Valid @Min(value = 1, message = "Pantry id must be greater than 0") long pantryId,
            @PathVariable(value = "productId") @Valid
            @Min(value = 1, message = "Pantry product id must be greater than 0") long pantryProductId,
            @RequestBody @Valid ReservePantryProductRequest reserveBody,
            Authentication authentication
    ) {
        PantryProductDTO response = this.pantryProductService
                .reservePantryProduct(pantryId, pantryProductId, reserveBody.reserved(), authentication.getName());

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
