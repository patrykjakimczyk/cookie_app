package com.cookie.app.service.impl;

import com.cookie.app.exception.UserWasNotFoundAfterAuthException;
import com.cookie.app.model.entity.User;
import com.cookie.app.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public abstract class AbstractCookieService {
    protected final UserRepository userRepository;

    protected AbstractCookieService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    protected User getUserByEmail(String userEmail) {
        Optional<User> userOptional = this.userRepository.findByEmail(userEmail);

        if (userOptional.isEmpty()) {
            throw new UserWasNotFoundAfterAuthException("User was not found in database after authentication");
        }

        return userOptional.get();
    }
}
