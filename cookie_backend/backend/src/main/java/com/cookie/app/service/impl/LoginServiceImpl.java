package com.cookie.app.service.impl;

import com.cookie.app.model.enums.Role;
import com.cookie.app.model.mapper.AuthorityMapper;
import com.cookie.app.model.request.RegistrationRequest;
import com.cookie.app.model.entity.User;
import com.cookie.app.model.response.LoginResponse;
import com.cookie.app.model.response.RegistrationResponse;
import com.cookie.app.repository.ProductRepository;
import com.cookie.app.repository.UserRepository;
import com.cookie.app.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public non-sealed class LoginServiceImpl extends AbstractCookieService implements LoginService {
    private final PasswordEncoder passwordEncoder;

    public LoginServiceImpl(UserRepository userRepository,
                            ProductRepository productRepository,
                            AuthorityMapper authorityMapper,
                            PasswordEncoder passwordEncoder) {
        super(userRepository, productRepository, authorityMapper);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public LoginResponse getLoginInfo(String email) {
        User user = super.getUserByEmail(email);

        return new LoginResponse(user.getUsername());
    }

    @Transactional
    @Override
    public RegistrationResponse userRegistration(RegistrationRequest request) {
        Optional<User> usernameOptional = super.userRepository.findByUsername(request.username());
        Optional<User> emailOptional = super.userRepository.findByEmail(request.email());
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
                .password(this.passwordEncoder.encode(request.password()))
                .creationDate(LocalDateTime.now())
                .birthDate(request.birthDate())
                .gender(request.gender())
                .build();

        this.userRepository.save(user);
        log.info("User for email={} has been successfully registered!", user.getEmail());

        return new RegistrationResponse(duplicatedFields);
    }
}
