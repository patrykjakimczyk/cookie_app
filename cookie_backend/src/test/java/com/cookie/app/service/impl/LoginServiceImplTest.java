package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.LoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
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
        List<String> duplicates = this.loginService.userRegistration(userToRegister);

        assertEquals(1, duplicates.size());
        assertEquals("username", duplicates.get(0));
    }

    @Test
    void test_userRegistrationUnSuccessfulEmailTaken() {
        RegistrationRequest userToRegister = new RegistrationRequest("username", "email", null, null, null);
        User user = User.builder().username("username2").email("email").build();

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        List<String> duplicates = this.loginService.userRegistration(userToRegister);

        assertEquals(1, duplicates.size());
        assertEquals("email", duplicates.get(0));
    }

    @Test
    void test_userRegistrationSuccessful() {
        RegistrationRequest userToRegister = new RegistrationRequest("username", "email", null, null, null);

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());
        List<String> duplicates = this.loginService.userRegistration(userToRegister);

        assertEquals(0, duplicates.size());
    }

    @Test
    void test_getUsernameSuccessful() {
        String email = "email";
        User user = User.builder().username("username").email("email").build();

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));
        String username = this.loginService.getUsername(email);

        assertEquals("username", username);
    }

    @Test
    void test_getUsernameThrowsError() {
        String email = "email";

        Mockito.when(this.userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(UserWasNotFoundAfterAuthException.class, () -> this.loginService.getUsername(email));
    }
}
