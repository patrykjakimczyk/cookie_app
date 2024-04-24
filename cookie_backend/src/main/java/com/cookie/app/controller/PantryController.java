package com.cookie.app.controller;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.service.PantryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/pantry", produces = { MediaType.APPLICATION_JSON_VALUE })
@RestController
public class PantryController {
    private static final String PANTRY_ID_URL = "/{pantryId}";
    private final PantryService pantryService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<GetPantryResponse> createPantry(
            @Valid @RequestBody CreatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is creating pantry for group with id={}", authentication.getName(), request.groupId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.pantryService.createPantry(request, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(PANTRY_ID_URL)
    public ResponseEntity<GetPantryResponse> getPantry(
            @PathVariable("pantryId") @Valid @Positive(message = "Id must be greater than 0") long pantryId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(this.pantryService.getPantry(pantryId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<GetUserPantriesResponse> getAllUserPantries(Authentication authentication) {
        return ResponseEntity.ok(this.pantryService.getAllUserPantries(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(PANTRY_ID_URL)
    public ResponseEntity<DeletePantryResponse> deletePantry(
            @PathVariable("pantryId") @Valid @Positive(message = "Id must be greater than 0") long pantryId,
            Authentication authentication
    ) {
        log.info("User with email={} is deleting pantry with id={}", authentication.getName(), pantryId);
        return ResponseEntity.ok(this.pantryService.deletePantry(pantryId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(PANTRY_ID_URL)
    public ResponseEntity<GetPantryResponse> updatePantry(
            @PathVariable("pantryId") @Valid @Positive(message = "Id must be greater than 0") long pantryId,
            @RequestBody @Valid UpdatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is updating pantry", authentication.getName());
        return ResponseEntity.ok(this.pantryService.updatePantry(pantryId, request, authentication.getName()));
    }
}
