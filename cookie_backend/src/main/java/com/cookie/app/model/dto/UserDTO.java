package com.cookie.app.model.dto;

import com.cookie.app.model.entity.Authority;

import java.util.List;
import java.util.Set;

public record UserDTO (
        Long id,
        String username,
        Set<Authority> authorities
){}
