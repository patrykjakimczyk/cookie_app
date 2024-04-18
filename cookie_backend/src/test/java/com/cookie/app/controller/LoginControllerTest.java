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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest extends AbstractControllerTest {
    final String email = "email";

    @Mock
    LoginService loginService;
    @InjectMocks
    LoginController controller;

    @Test
    void test_loginWithAuthenticatedUser() {

        when(loginService.getLoginInfo(Mockito.anyString())).thenReturn(new LoginResponse(username));

        ResponseEntity<LoginResponse> response = this.controller.login(authentication);
        assertEquals(username, response.getBody().username());
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void test_loginUserWasNotFoundAfterAuthentication() {

        when(loginService.getLoginInfo(Mockito.anyString())).thenThrow(new UserWasNotFoundAfterAuthException("User not found after authorization"));

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.controller.login(authentication));
    }

    @Test
    void test_registerUserSuccessfully() {
        final RegistrationRequest request = new RegistrationRequest(username, email, password, null, null);

        when(loginService.userRegistration(Mockito.any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResponse(Collections.emptyList()));
        ResponseEntity<RegistrationResponse> response = this.controller.registerUser(request);

        assertEquals(0, response.getBody().duplicates().size());
        assertSame(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void test_registerUserDuplicates() {
        final RegistrationRequest request = new RegistrationRequest(username, email, password, null, null);
        final String duplicate = username;

        when(loginService.userRegistration(Mockito.any(RegistrationRequest.class)))
                .thenReturn(new RegistrationResponse(List.of(duplicate)));
        ResponseEntity<RegistrationResponse> response = this.controller.registerUser(request);

        assertEquals(1, response.getBody().duplicates().size());
        assertEquals(duplicate, response.getBody().duplicates().get(0));
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
