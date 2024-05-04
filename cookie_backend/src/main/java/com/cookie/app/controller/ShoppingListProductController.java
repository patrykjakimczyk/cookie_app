package com.cookie.app.controller;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.service.ShoppingListProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/shopping-lists/{listId}/products", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
@RestController
public class ShoppingListProductController {
    private static final String LIST_PRODUCTS_TRANSFER_URL = "/transfer";
    private static final String LIST_PRODUCTS_PURCHASE_URL = "/purchase";
    private static final String LIST_PRODUCTS_PAGE_URL = "/{page}";

    private final ShoppingListProductService shoppingListProductService;

    @Operation(summary = "Get shopping list products")
    @ApiResponse(responseCode = "200", description = "shopping list returned",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(LIST_PRODUCTS_PAGE_URL)
    public ResponseEntity<PageResult<ShoppingListProductDTO>> getShoppingListProducts(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @PathVariable @Positive(message = "Page nr must be at least 0") int page,
            @ParameterObject FilterRequest filterRequest,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.shoppingListProductService.getShoppingListProducts(
                        listId,
                        page,
                        filterRequest,
                        authentication.getName()
                )
        );
    }

    @Operation(summary = "Add products to shopping list")
    @ApiResponse(responseCode = "201", description = "Products added to shopping list",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<Void> addProductsToShoppingList(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @Valid @NotEmpty(message = "List of products cannot be empty")
            @RequestBody List<@Valid ShoppingListProductDTO> products,
            Authentication authentication
    ) {
        this.shoppingListProductService.addProductsToShoppingList(listId, products, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @Operation(summary = "Remove products from shopping list")
    @ApiResponse(responseCode = "200", description = "Products removed from shopping list",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> removeProductsFromShoppingList(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @Valid @NotEmpty(message = "List of ids cannot be empty")
            @RequestBody List<@Positive(message = "Product id must be greater than 0") Long> productIds,
            Authentication authentication
    ) {
        this.shoppingListProductService.removeProductsFromShoppingList(listId, productIds, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update shopping list product")
    @ApiResponse(responseCode = "200", description = "Shopping list product updated",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping
    public ResponseEntity<Void> updateShoppingListProduct(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @Valid @RequestBody ShoppingListProductDTO shoppingListProductDTO,
            Authentication authentication
    ) {
        this.shoppingListProductService.updateShoppingListProduct(listId, shoppingListProductDTO, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change purchase status for shopping list products")
    @ApiResponse(responseCode = "200", description = "Purchase statuses changed for shopping list products",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(LIST_PRODUCTS_PURCHASE_URL)
    public ResponseEntity<Void> changePurchaseStatusForProducts(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @Valid @NotEmpty(message = "List of ids cannot be empty")
            @RequestBody List<@Positive(message = "Product id must be greater than 0") Long> productIds,
            Authentication authentication
    ) {
        this.shoppingListProductService.changePurchaseStatusForProducts(listId, productIds, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Transfer shopping list products to pantry")
    @ApiResponse(responseCode = "200", description = "Shopping list products transfered to pantry",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(LIST_PRODUCTS_TRANSFER_URL)
    public ResponseEntity<Void> transferProductsToPantry(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            Authentication authentication
    ) {
        log.info("User with email={} is transfering products from shopping list with id={} to pantry", authentication.getName(), listId);
        this.shoppingListProductService.transferProductsToPantry(listId, authentication.getName());

        return ResponseEntity.ok().build();
    }
}


