package com.cookie.app.controller;

import com.cookie.app.model.request.CreatePantryRequest;
import com.cookie.app.model.response.GetPantryResponse;
import com.cookie.app.service.PantryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class PantryController {
    private static final String PANTRY_URL = "/pantry";
    private final PantryService pantryService;

    @PostMapping(PANTRY_URL)
    public ResponseEntity<Void> createPantry(@Valid CreatePantryRequest request, Authentication authentication) {
        this.pantryService.createPantry(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @GetMapping(PANTRY_URL)
    public ResponseEntity<GetPantryResponse> getPantry(@Valid CreatePantryRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.OK).body(this.pantryService.getUserPantry(authentication.getName()));
    }
}
