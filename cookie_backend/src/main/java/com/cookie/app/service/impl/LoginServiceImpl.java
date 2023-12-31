package com.cookie.app.service.impl;

import com.cookie.app.model.enums.Role;
import com.cookie.app.model.mapper.AuthorityMapperDTO;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
public class LoginServiceImpl extends AbstractCookieService implements LoginService {
    private final PasswordEncoder passwordEncoder;

    public LoginServiceImpl(
            UserRepository userRepository,
            AuthorityMapperDTO authorityMapperDTO,
            PasswordEncoder passwordEncoder) {
        super(userRepository, authorityMapperDTO);
        this.passwordEncoder = passwordEncoder;
    }

    public RegistrationResponse userRegistration(RegistrationRequest request) {
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
            return new RegistrationResponse(duplicatedFields);
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

        return new RegistrationResponse(duplicatedFields);
    }

    @Override
    public LoginResponse getLoginInfo(String email) {
        User user = this.getUserByEmail(email);

        return new LoginResponse(user.getUsername());
    }
}
