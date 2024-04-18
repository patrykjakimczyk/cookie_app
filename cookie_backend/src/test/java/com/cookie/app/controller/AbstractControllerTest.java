package com.cookie.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Collections;

abstract class AbstractControllerTest {
    protected final String username = "username";
    protected final String password = "password";

    protected Authentication authentication;

    @BeforeEach
    void init() {
        this.authentication = new UsernamePasswordAuthenticationToken(username, password, Collections.emptyList());
    }
}
