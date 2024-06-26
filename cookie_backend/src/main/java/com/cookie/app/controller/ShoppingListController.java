package com.cookie.app.controller;

import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.request.CreateShoppingListRequest;
import com.cookie.app.model.request.UpdateShoppingListRequest;
import com.cookie.app.model.response.DeleteShoppingListResponse;
import com.cookie.app.model.response.GetShoppingListResponse;
import com.cookie.app.model.response.GetUserShoppingListsResponse;
import com.cookie.app.service.ShoppingListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/shopping-lists", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
@RestController
public class ShoppingListController {
    private static final String SHOPPING_LIST_ID_URL = "/{listId}";

    private final ShoppingListService shoppingListService;


    @Operation(summary = "Create shopping list")
    @ApiResponse(responseCode = "201", description = "Shopping list created",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetShoppingListResponse.class)) })
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

    @Operation(summary = "Get shopping list")
    @ApiResponse(responseCode = "200", description = "Shopping list returned",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetShoppingListResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(SHOPPING_LIST_ID_URL)
    public ResponseEntity<GetShoppingListResponse> getShoppingList(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.shoppingListService.getShoppingList(listId, authentication.getName()));
    }


    @Operation(summary = "Get all user's shopping lists")
    @ApiResponse(responseCode = "200", description = "All user's shopping lists returned",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetUserShoppingListsResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<GetUserShoppingListsResponse> getAllUserShoppingLists(
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.shoppingListService.getUserShoppingLists(authentication.getName()));
    }


    @Operation(summary = "Delete shopping list")
    @ApiResponse(responseCode = "200", description = "Shopping list deleted",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DeleteShoppingListResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(SHOPPING_LIST_ID_URL)
    public ResponseEntity<DeleteShoppingListResponse> deleteShoppingList(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting shopping list with id={}", authentication.getName(), listId);
        return ResponseEntity.ok(this.shoppingListService.deleteShoppingList(listId, authentication.getName()));
    }


    @Operation(summary = "Update shopping list")
    @ApiResponse(responseCode = "200", description = "Shopping list updated",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetShoppingListResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(SHOPPING_LIST_ID_URL)
    public ResponseEntity<GetShoppingListResponse> updateShoppingList(
            @PathVariable @Positive(message = "Shopping list id must be greater than 0") long listId,
            @RequestBody @Valid UpdateShoppingListRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is updating shopping list with id={}", authentication.getName(), listId);
        return ResponseEntity.ok(this.shoppingListService.updateShoppingList(listId, request, authentication.getName()));
    }
}
