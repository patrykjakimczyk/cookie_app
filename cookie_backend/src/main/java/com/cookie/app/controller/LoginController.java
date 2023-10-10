package com.cookie.app.controller;

import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.service.LoginService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@Slf4j
@RestController
public class LoginController {
    private final LoginService loginService;

    @GetMapping("/")
    public String get() {
        return "Hello world";
    }

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(@Valid @RequestBody RegistrationRequest request) {
        log.info("Performing user registration for email {}", request.email());
        this.loginService.userRegistration(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
}

