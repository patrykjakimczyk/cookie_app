package com.cookie.app.controller;

import com.cookie.app.model.RegexConstants;
import com.cookie.app.model.dto.PageResult;
import com.cookie.app.model.dto.PantryProductDTO;
import com.cookie.app.model.request.FilterRequest;
import com.cookie.app.model.request.ReservePantryProductRequest;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.service.PantryProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
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
@RequestMapping(value = "/api/v1/pantries/{pantryId}/products", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
@RestController
public class PantryProductController {
    private static final String GET_PANTRY_PRODUCTS_URL = "/{page}";
    private static final String PANTRY_PRODUCT_URL = "/{pantryProductId}";

    private final PantryProductService pantryProductService;

    @Operation(summary = "Get pantry products")
    @ApiResponse(responseCode = "200", description = "Pantry products returned",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(GET_PANTRY_PRODUCTS_URL)
    public ResponseEntity<PageResult<PantryProductDTO>> getPantryProducts(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            @PathVariable @Positive(message = "Page nr must be be greater than 0") int page,
            @ParameterObject FilterRequest filterRequest,
            Authentication authentication
    ) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(this.pantryProductService.getPantryProducts(
                        pantryId, page, filterRequest, authentication.getName())
                );
    }

    @Operation(summary = "Add products to pantry")
    @ApiResponse(responseCode = "201", description = "Products added to pantry",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<Void> addProductsToPantry(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            @Valid @NotEmpty(message = "List of products cannot be empty") @RequestBody List<@Valid PantryProductDTO> products,
            Authentication authentication
    ) {
        this.pantryProductService.addProductsToPantry(pantryId, products, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Remove products from pantry")
    @ApiResponse(responseCode = "200", description = "Products removed from pantry",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping
    public ResponseEntity<Void> removeProductsFromPantry(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            @Valid @NotEmpty(message = "List of products ids cannot be empty")
            @RequestBody List<@Positive(message = "Product id must be greater than 0") Long> productIds,
            Authentication authentication
    ) {
        this.pantryProductService.removeProductsFromPantry(pantryId, productIds, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update pantry product")
    @ApiResponse(responseCode = "200", description = "Pantry product updated",
            content = { @Content(mediaType = "application/json") })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping
    public ResponseEntity<Void> updatePantryProduct(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            @RequestBody @Valid PantryProductDTO pantryProductDTO,
            Authentication authentication
    ) {
        this.pantryProductService.updatePantryProduct(pantryId, pantryProductDTO, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reserve pantry product")
    @ApiResponse(responseCode = "200", description = "Pantry product reserved",
            content = { @Content(mediaType = "application/json",
                schema = @Schema(implementation = PantryProductDTO.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(PANTRY_PRODUCT_URL)
    public ResponseEntity<PantryProductDTO> reservePantryProduct(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            @PathVariable @Positive(message = "Pantry product id must be greater than 0") long pantryProductId,
            @RequestBody @Valid ReservePantryProductRequest reserveBody,
            Authentication authentication
    ) {
        PantryProductDTO response = this.pantryProductService
                .reservePantryProduct(pantryId, pantryProductId, reserveBody.reserved(), authentication.getName());

        return ResponseEntity.ok(response);
    }
}
