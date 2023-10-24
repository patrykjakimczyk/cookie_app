package com.cookie.app.controller;

import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.service.LoginService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
@RestController
public class LoginController {
    private static final String LOGIN_URL = "/login";
    private static final String REGISTRATION_URL = "/register";
    private final LoginService loginService;

    @SecurityRequirement(name = "basicAuth")
    @GetMapping(LOGIN_URL)
    public String login() {
        return "Hello world";
    }

    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @PostMapping(REGISTRATION_URL)
    public ResponseEntity<RegistrationResponse> registerUser(@Valid @RequestBody RegistrationRequest request) {
        log.info("Performing user registration for email {}", request.email());
        RegistrationResponse response = new RegistrationResponse(this.loginService.userRegistration(request));

        if (response.duplicates().isEmpty()) {
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}

