package com.cookie.app.controller;

import com.cookie.app.model.dto.ShoppingListProductDTO;
import com.cookie.app.service.ShoppingListProductService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ShoppingListProductController {
    private static final String LIST_PRODUCTS_PAGE_URL = "/shopping-list/{id}/products/{page}";

    private final ShoppingListProductService shoppingListProductService;

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(LIST_PRODUCTS_PAGE_URL)
    public ResponseEntity<Page<ShoppingListProductDTO>> getShoppingListProducts(
            @PathVariable(value = "id") @Valid @Min(1) long id,
            @PathVariable(value = "page") @Valid @Min(0) int page,
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
}
