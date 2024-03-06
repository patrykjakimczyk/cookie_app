package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {
    final String email = "email@email.com";
    final String username = "username";
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    LoginServiceImpl loginService;

    @Test
    void test_userRegistrationUnSuccessfulUsernameTaken() {
        RegistrationRequest userToRegister = new RegistrationRequest(username, email, null, null, null);
        User user = User.builder().username(username).email("email2").build();

        doReturn(Optional.of(user)).when(userRepository).findByUsername(Mockito.anyString());
        RegistrationResponse response = this.loginService.userRegistration(userToRegister);

        assertEquals(1, response.duplicates().size());
        assertEquals("username", response.duplicates().get(0));
    }

    @Test
    void test_userRegistrationUnSuccessfulEmailTaken() {
        RegistrationRequest userToRegister = new RegistrationRequest(username, email, null, null, null);
        User user = User.builder().username("username2").email(email).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(Mockito.anyString());
        RegistrationResponse response = this.loginService.userRegistration(userToRegister);

        assertEquals(1, response.duplicates().size());
        assertEquals("email", response.duplicates().get(0));
    }

    @Test
    void test_userRegistrationSuccessful() {
        RegistrationRequest userToRegister = new RegistrationRequest(username, email, null, null, null);

        doReturn(Optional.empty()).when(userRepository).findByUsername(Mockito.anyString());
        RegistrationResponse response = this.loginService.userRegistration(userToRegister);

        assertEquals(0, response.duplicates().size());
    }

    @Test
    void test_getUsernameSuccessful() {
        User user = User.builder().username(username).email(email).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(Mockito.anyString());
        LoginResponse response = this.loginService.getLoginInfo(email);

        assertEquals("username", response.username());
    }

    @Test
    void test_getUsernameThrowsError() {

        doReturn(Optional.empty()).when(userRepository).findByEmail(Mockito.anyString());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.loginService.getLoginInfo(email));
    }
}
