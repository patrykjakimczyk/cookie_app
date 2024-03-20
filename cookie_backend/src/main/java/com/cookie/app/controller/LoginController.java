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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
@RestController
public class LoginController {
    private static final String USER_URL = "/user";
    private final LoginService loginService;

    @SecurityRequirement(name = "basicAuth")
    @GetMapping(USER_URL)
    public ResponseEntity<LoginResponse> login(Authentication auth) {
        LoginResponse response = this.loginService.getLoginInfo(auth.getName());
        return ResponseEntity.ok(response);
    }

    @PostMapping(USER_URL)
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationRequest request) {
        log.info("Performing user registration for email={}", request.email());
        RegistrationResponse response = this.loginService.userRegistration(request);

        if (response.duplicates().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        return ResponseEntity.ok(response);
    }
}

