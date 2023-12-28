package com.cookie.app.service;

import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class AuthoritiesExtractorService {
    public void extractAuthoritiesOnlyForGroup(User user, Group group) {
        user.setAuthorities(
                user.getAuthorities()
                        .stream()
                        .filter(authority -> authority.getGroup().getId() == group.getId())
                        .collect(Collectors.toSet())
        );
    }
}
