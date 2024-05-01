package com.cookie.app.controller;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.service.ShoppingListProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(LIST_PRODUCTS_PAGE_URL)
    public ResponseEntity<PageResult<ShoppingListProductDTO>> getShoppingListProducts(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @PathVariable @Positive(message = "Page nr must be at least 0") int page,
            @RequestParam(required = false) @Pattern(
                    regexp = RegexConstants.FILTER_VALUE_REGEX,
                    message = "Filter value can only contains letters, digits, whitespaces, dashes and its length must be greater than 0"
            ) String filterValue,
            @RequestParam(required = false) @Pattern(
                    regexp = RegexConstants.SORT_COL_REGEX,
                    message = "Filter value can only contains letters, underscores and its length must be greater than 0"
            ) String sortColName,
            @RequestParam(required = false) Sort.Direction sortDirection,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.shoppingListProductService.getShoppingListProducts(
                        listId,
                        page,
                        filterValue,
                        sortColName,
                        sortDirection,
                        authentication.getName()
                )
        );
    }

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


