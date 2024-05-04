package com.cookie.app.controller;

import com.cookie.app.model.dto.MealDTO;
import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.service.PantryService;
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
@RequestMapping(value = "/api/v1/pantries", produces = { MediaType.APPLICATION_JSON_VALUE })
@Validated
@RestController
public class PantryController {
    private static final String PANTRY_ID_URL = "/{groupId}";

    private final PantryService pantryService;

    @Operation(summary = "Create pantry")
    @ApiResponse(responseCode = "201", description = "Pantry created",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetPantryResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<GetPantryResponse> createPantry(
            @RequestBody @Valid CreatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is creating pantry for group with id={}", authentication.getName(), request.groupId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.pantryService.createPantry(request, authentication.getName()));
    }

    @Operation(summary = "Get pantry")
    @ApiResponse(responseCode = "200", description = "Pantry returned",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetPantryResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(PANTRY_ID_URL)
    public ResponseEntity<GetPantryResponse> getPantry(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.pantryService.getPantry(pantryId, authentication.getName()));
    }

    @Operation(summary = "Get all user's pantries")
    @ApiResponse(responseCode = "200", description = "All user's pantries returned",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetUserPantriesResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<GetUserPantriesResponse> getAllUserPantries(Authentication authentication) {
        return ResponseEntity.ok(this.pantryService.getAllUserPantries(authentication.getName()));
    }

    @Operation(summary = "Delete pantry")
    @ApiResponse(responseCode = "200", description = "Pantry deleted",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = DeletePantryResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(PANTRY_ID_URL)
    public ResponseEntity<DeletePantryResponse> deletePantry(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting pantry with id={}", authentication.getName(), pantryId);
        return ResponseEntity.ok(this.pantryService.deletePantry(pantryId, authentication.getName()));
    }

    @Operation(summary = "Update pantry")
    @ApiResponse(responseCode = "200", description = "Pantry updated",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = GetPantryResponse.class)) })
    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(PANTRY_ID_URL)
    public ResponseEntity<GetPantryResponse> updatePantry(
            @PathVariable @Positive(message = "Pantry id must be greater than 0") long pantryId,
            @RequestBody @Valid UpdatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is updating pantry", authentication.getName());
        return ResponseEntity.ok(this.pantryService.updatePantry(pantryId, request, authentication.getName()));
    }
}
