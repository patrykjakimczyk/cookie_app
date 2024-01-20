package com.cookie.app.model.mapper;

import com.cookie.app.model.entity.Group;
import com.cookie.app.model.entity.User;

import java.util.stream.Collectors;

public interface AuthoritiesExtractor {

    default void extractAuthoritiesOnlyForGroup(User user, Group group) {
        user.setAuthorities(
                user.getAuthorities()
                        .stream()
                        .filter(authority -> authority.getGroup().getId() == group.getId())
                        .collect(Collectors.toSet())
        );
    }
}
