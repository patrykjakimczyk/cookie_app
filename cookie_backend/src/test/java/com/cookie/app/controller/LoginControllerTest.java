package com.cookie.app.controller;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(MockitoExtension.class)
class LoginControllerTest {
    @Mock
    LoginService loginService;
    @InjectMocks
    LoginController loginController;

    @Test
    void test_loginWithAuthenticatedUser() {
        final Authentication authentication = new UsernamePasswordAuthenticationToken("username", "password", Collections.emptyList());

        Mockito.when(loginService.getUsername(Mockito.anyString())).thenReturn("username");

        ResponseEntity<LoginResponse> response = this.loginController.login(authentication);
        assertTrue(response.getBody().username().equals("username"));
        assertTrue(response.getStatusCode() == HttpStatus.OK);
    }

    @Test
    void test_loginUserWasNotFoundAfterAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken("username", "password", Collections.emptyList());

        Mockito.when(loginService.getUsername(Mockito.anyString())).thenThrow(new UserWasNotFoundAfterAuthException("User not found after authorization"));

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.loginController.login(authentication));
    }

    @Test
    void test_registerUserSuccessfully() {
        RegistrationRequest request = new RegistrationRequest("user", "email", "pass", null, null);

        Mockito.when(loginService.userRegistration(Mockito.any(RegistrationRequest.class)))
                .thenReturn(Collections.emptyList());

        ResponseEntity<RegistrationResponse> response = this.loginController.registerUser(request);
        assertTrue(response.getBody().duplicates().size() == 0);
        assertTrue(response.getStatusCode() == HttpStatus.CREATED);
    }

    @Test
    void test_registerUserDuplicates() {
        RegistrationRequest request = new RegistrationRequest("user", "email", "pass", null, null);
        String duplicate = "username";

        Mockito.when(loginService.userRegistration(Mockito.any(RegistrationRequest.class)))
                .thenReturn(List.of(duplicate));

        ResponseEntity<RegistrationResponse> response = this.loginController.registerUser(request);
        assertEquals(1, response.getBody().duplicates().size());
        assertEquals(duplicate, response.getBody().duplicates().get(0));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
