package com.cookie.app.controller;

import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.service.LoginService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping(value = "/api/v1/user", produces = { MediaType.APPLICATION_JSON_VALUE })
@RestController
public class LoginController {
    private final LoginService loginService;

    @SecurityRequirement(name = "basicAuth")
    @GetMapping
    public ResponseEntity<LoginResponse> login(Authentication auth) {
        return ResponseEntity.ok(this.loginService.getLoginInfo(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationRequest request) {
        log.info("Performing creator registration for email={}", request.email());
        RegistrationResponse response = this.loginService.userRegistration(request);

        if (response.duplicates().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        return ResponseEntity.ok(response);
    }
}

