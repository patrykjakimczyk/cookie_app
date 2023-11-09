package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.Pantry;
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

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {
    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;
    @InjectMocks
    LoginServiceImpl loginService;

    @Test
    void test_userRegistrationUnSuccessfulUsernameTaken() {
        RegistrationRequest userToRegister = new RegistrationRequest("username", "email", null, null, null);
        User user = User.builder().username("username").email("email2").build();

        Mockito.when(this.userRepository.findByUsername(Mockito.anyString())).thenReturn(Optional.of(user));
        RegistrationResponse response = this.loginService.userRegistration(userToRegister);

        assertEquals(1, response.duplicates().size());
        assertEquals("username", response.duplicates().get(0));
    }

    @Test
    void test_userRegistrationUnSuccessfulEmailTaken() {
        RegistrationRequest userToRegister = new RegistrationRequest("username", "email", null, null, null);
        User user = User.builder().username("username2").email("email").build();

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        RegistrationResponse response = this.loginService.userRegistration(userToRegister);

        assertEquals(1, response.duplicates().size());
        assertEquals("email", response.duplicates().get(0));
    }

    @Test
    void test_userRegistrationSuccessful() {
        RegistrationRequest userToRegister = new RegistrationRequest("username", "email", null, null, null);

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        RegistrationResponse response = this.loginService.userRegistration(userToRegister);

        assertEquals(0, response.duplicates().size());
    }

    @Test
    void test_getUsernameSuccessful() {
        String email = "email";
        Pantry pantry = new Pantry();
        User user = User.builder().username("username").email("email").pantry(pantry).build();

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        LoginResponse response = this.loginService.getLoginInfo(email);

        assertEquals("username", response.username());
        assertEquals(true, response.assignedPantry());
    }

    @Test
    void test_getUsernameThrowsError() {
        String email = "email";

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.loginService.getLoginInfo(email));
    }
}
