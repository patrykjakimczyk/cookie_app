package com.cookie.app.service.impl;

import com.cookie.app.exception.NotUniqueValueException;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.entity.User;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void userRegistration(RegistrationRequest request) {
        User user = User
                .builder()
                .role(request.role())
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .activated(false)
                .creationDate(Instant.now())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .build();

        try {
            this.userRepository.save(user);
            log.info("User has been successfully registered!");
        } catch (RuntimeException e) {
            log.warn("Registration process failed. User used already taken email/username");
            throw new NotUniqueValueException("Error has occurred during user's registration. E-mail/username isn't unique", e.getCause());
        }
    }
}
