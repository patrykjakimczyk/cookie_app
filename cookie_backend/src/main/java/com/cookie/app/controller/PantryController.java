package com.cookie.app.controller;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.model.response.GetUserPantriesResponse;
import com.cookie.app.service.PantryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RestController
public class PantryController {
    private static final String PANTRIES_URL = "/pantries";
    private static final String PANTRY_URL = "/pantry";
    private static final String PANTRY_ID_URL = "/pantry/{id}";
    private final PantryService pantryService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(PANTRY_URL)
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
    public ResponseEntity<GetPantryResponse> getPantry(@PathVariable("id") long pantryId, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.getPantry(pantryId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(PANTRIES_URL)
    public ResponseEntity<GetUserPantriesResponse> getAllUserPantries(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.getAllUserPantries(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(PANTRY_ID_URL)
    public ResponseEntity<DeletePantryResponse> deletePantry(@PathVariable("id") long pantryId, Authentication authentication) {
        log.info("User with email={} is deleting pantry with id={}", authentication.getName(), pantryId);
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.deletePantry(pantryId, authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(PANTRY_ID_URL)
    public ResponseEntity<GetPantryResponse> updatePantry(
            @PathVariable("id") long pantryId,
            @RequestBody UpdatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is updating pantry", authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.updatePantry(pantryId, request, authentication.getName()));
    }
}
