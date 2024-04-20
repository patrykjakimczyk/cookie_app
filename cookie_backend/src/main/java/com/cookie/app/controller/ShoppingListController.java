package com.cookie.app.controller;

import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;
import com.cookie.app.service.ShoppingListService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/shopping-lists", produces = { MediaType.APPLICATION_JSON_VALUE })
@RestController
public class ShoppingListController {
    private static final String SHOPPING_LIST_ID_URL = "/{id}";
    private final ShoppingListService shoppingListService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<GetShoppingListResponse> createShoppingList(
            @Valid @RequestBody CreateShoppingListRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is creating shopping list for group with id={}", authentication.getName(), request.groupId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.shoppingListService.createShoppingList(request, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(SHOPPING_LIST_ID_URL)
    public ResponseEntity<GetShoppingListResponse> getShoppingList(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long listId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.shoppingListService.getShoppingList(listId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<GetUserShoppingListsResponse> getAllUserShoppingLists(
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.shoppingListService.getUserShoppingLists(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(SHOPPING_LIST_ID_URL)
    public ResponseEntity<DeleteShoppingListResponse> deleteShoppingList(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long listId,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting shopping list with id={}", authentication.getName(), listId);
        return ResponseEntity.ok(this.shoppingListService.deleteShoppingList(listId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(SHOPPING_LIST_ID_URL)
    public ResponseEntity<GetShoppingListResponse> updateShoppingList(
            @PathVariable("id") @Valid @Min(value = 1, message = "Id must be greater than 0") long listId,
            @RequestBody @Valid UpdateShoppingListRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is updating shopping list with id={}", authentication.getName(), listId);
        return ResponseEntity.ok(this.shoppingListService.updateShoppingList(listId, request, authentication.getName()));
    }
}
