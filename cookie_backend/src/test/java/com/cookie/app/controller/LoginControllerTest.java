package com.cookie.app.controller;

import com.cookie.app.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class)
class LoginControllerTest {
    @MockBean
    private LoginService loginService;

    @Autowired
    MockMvc mvc;

    @Test
    @WithMockUser
    void test_loginWithAuthenticatedUser() throws Exception {
        this.mvc
                .perform(get("/user"))
                .andExpect(status().isOk());
    }

    @Test
    void test_loginWithoutAuthentication() throws Exception {
        this.mvc
                .perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }
}
