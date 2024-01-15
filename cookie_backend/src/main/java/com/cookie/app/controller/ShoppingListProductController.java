package com.cookie.app.controller;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.service.ShoppingListProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Validated
@Slf4j
@RestController
public class ShoppingListProductController {
    private static final String LIST_PRODUCTS_URL = "/shopping-list/{id}/products";
    private static final String LIST_PRODUCTS_TRANSFER_URL = "/shopping-list/{id}/products/transfer";
    private static final String LIST_PRODUCTS_PURCHASE_URL = "/shopping-list/{id}/products/purchase";
    private static final String LIST_PRODUCTS_PAGE_URL = "/shopping-list/{id}/products/{page}";

    private final ShoppingListProductService shoppingListProductService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(LIST_PRODUCTS_PAGE_URL)
    public ResponseEntity<Page<ShoppingListProductDTO>> getShoppingListProducts(
            @PathVariable(value = "id") @Valid @Min(value = 1, message = "Id must be greater than 0") long id,
            @PathVariable(value = "page") @Valid @Min(value = 0, message = "Page nr must be at least 0") int page,
            @RequestParam String filterValue,
            @RequestParam String sortColName,
            @RequestParam String sortDirection,
            Authentication authentication
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.shoppingListProductService.getShoppingListProducts(
                    id,
                    page,
                    filterValue,
                    sortColName,
                    sortDirection,
                    authentication.getName()
                )
        );
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(LIST_PRODUCTS_URL)
    public ResponseEntity<Void> addProductsToShoppingList(
            @PathVariable(value = "id") @Valid @Min(value = 1, message = "Id must be greater than 0") long id,
            @Valid @NotEmpty(message = "List of products cannot be empty") @RequestBody List<ShoppingListProductDTO> products,
            Authentication authentication
    ) {
        this.shoppingListProductService.addProductsToShoppingList(id, products, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(LIST_PRODUCTS_URL)
    public ResponseEntity<Void> removeProductsFromShoppingList(
            @PathVariable(value = "id") @Valid @Min(value = 1, message = "Id must be greater than 0") long id,
            @Valid @NotEmpty(message = "List of ids cannot be empty") @RequestBody List<Long> productIds,
            Authentication authentication
    ) {
        this.shoppingListProductService.removeProductsFromShoppingList(id, productIds, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(LIST_PRODUCTS_URL)
    public ResponseEntity<Void> modifyShoppingListProduct(
            @PathVariable(value = "id") @Valid @Min(value = 1, message = "Id must be greater than 0") long id,
            @Valid @RequestBody ShoppingListProductDTO shoppingListProductDTO,
            Authentication authentication
    ) {
        this.shoppingListProductService.modifyShoppingListProduct(id, shoppingListProductDTO, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(LIST_PRODUCTS_PURCHASE_URL)
    public ResponseEntity<Void> changePurchaseStatusForProducts(
            @PathVariable(value = "id") @Valid @Min(value = 1, message = "Id must be greater than 0") long id,
            @Valid @NotEmpty(message = "List of ids cannot be empty") @RequestBody List<Long> productIds,
            Authentication authentication
    ) {
        this.shoppingListProductService.changePurchaseStatusForProducts(id, productIds, authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(LIST_PRODUCTS_TRANSFER_URL)
    public ResponseEntity<Void> transferProductsToPantry(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long id,
            Authentication authentication
    ) {
        log.info("User with email={} is transfering products from shopping list with id={} to pantry", authentication.getName(), id);
        this.shoppingListProductService.transferProductsToPantry(id, authentication.getName());

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
