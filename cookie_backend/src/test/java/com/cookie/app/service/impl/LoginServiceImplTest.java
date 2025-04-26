package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.enums.Gender;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
    LoginServiceImpl service;

    @Test
    void test_userRegistrationUnSuccessfulUsernameTaken() {
        final RegistrationRequest userToRegister = new RegistrationRequest(username, email, null, null, null);
        final User user = User.builder().username(username).email("email2").build();

        doReturn(Optional.of(user)).when(userRepository).findByUsername(userToRegister.username());
        RegistrationResponse response = this.service.userRegistration(userToRegister);

        assertThat(response.duplicates()).hasSize(1);
        assertThat(response.duplicates().get(0)).isEqualTo("username");
    }

    @Test
    void test_userRegistrationUnSuccessfulEmailTaken() {
        final RegistrationRequest userToRegister = new RegistrationRequest(username, email, null, null, null);
        final User user = User.builder().username("username2").email(email).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(userToRegister.email());
        RegistrationResponse response = this.service.userRegistration(userToRegister);

        assertThat(response.duplicates()).hasSize(1);
        assertThat(response.duplicates().get(0)).isEqualTo("email");
    }

    @Test
    void test_userRegistrationSuccessful() {
        final RegistrationRequest userToRegister = new RegistrationRequest(username, email, null, null, Gender.MALE);

        doReturn(Optional.empty()).when(userRepository).findByUsername(userToRegister.username());
        RegistrationResponse response = this.service.userRegistration(userToRegister);

        assertThat(response.duplicates()).isEmpty();
    }

    @Test
    void test_getUsernameSuccessful() {
        final User user = User.builder().username(username).email(email).build();

        doReturn(Optional.of(user)).when(userRepository).findByEmail(email);
        LoginResponse response = this.service.getLoginInfo(email);

        assertThat(response.username()).isEqualTo("username");
    }

    @Test
    void test_getUsernameThrowsException() {
        doReturn(Optional.empty()).when(userRepository).findByEmail(email);

        assertThatThrownBy(() -> this.service.getLoginInfo(email))
                .isInstanceOf(UserWasNotFoundAfterAuthException.class);
    }
}
