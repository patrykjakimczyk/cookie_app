package com.cookie.app.controller;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.request.UpdatePantryRequest;
import com.cookie.app.model.response.DeletePantryResponse;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.service.PantryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
public class PantryController {
    private static final String PANTRY_URL = "/pantry";
    private final PantryService pantryService;

    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(PANTRY_URL)
    public ResponseEntity<Void> createUserPantry(
            @Valid @RequestBody CreatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is creating pantry", authentication.getName());
        this.pantryService.createUserPantry(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping(PANTRY_URL)
    public ResponseEntity<GetPantryResponse> getUserPantry(Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.getUserPantry(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping(PANTRY_URL)
    public ResponseEntity<DeletePantryResponse> deleteUserPantry(Authentication authentication) {
        log.info("User with email={} is deleting pantry", authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.deleteUserPantry(authentication.getName()));
    }

    @SecurityRequirement(name = "bearerAuth")
    @PatchMapping(PANTRY_URL)
    public ResponseEntity<GetPantryResponse> updateUserPantry(
            @RequestBody UpdatePantryRequest request,
            Authentication authentication
    ) {
        log.info("User with email={} is updating pantry", authentication.getName());
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.updateUserPantry(request, authentication.getName()));
    }
}
