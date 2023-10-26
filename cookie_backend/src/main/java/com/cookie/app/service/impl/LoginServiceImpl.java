package com.cookie.app.service.impl;

import com.cookie.app.exception.NotUniqueValueException;
import com.cookie.app.model.enums.Role;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.entity.User;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class LoginServiceImpl implements LoginService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public List<String> userRegistration(RegistrationRequest request) {
        Optional<User> usernameOptional = this.userRepository.findByUsername(request.username());
        Optional<User> emailOptional = this.userRepository.findByEmail(request.email());
        final List<String> duplicatedFields = new ArrayList<>();

        if (usernameOptional.isPresent()) {
            duplicatedFields.add("username");
        }

        if (emailOptional.isPresent()) {
            duplicatedFields.add("email");
        }

        if (!duplicatedFields.isEmpty()) {
            return duplicatedFields;
        }

        User user = User
                .builder()
                .role(Role.USER)
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .activated(false)
                .creationDate(Timestamp.from(Instant.now()))
                .birthDate(request.birthDate())
                .gender(request.gender())
                .build();

        this.userRepository.save(user);
        log.info("User has been successfully registered!");

        return duplicatedFields;
    }

    @Override
    public String getUsername(String email) {
        Optional<User> userOptional = this.userRepository.findByEmail(email);

        return userOptional.get().getUsername();  //user must be present if authentication passed
    }
}
