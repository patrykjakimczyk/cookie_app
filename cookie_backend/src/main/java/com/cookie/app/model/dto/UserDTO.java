package com.cookie.app.model.dto;


import java.util.Set;

public record UserDTO (
        Long id,
        String username,
        Set<AuthorityDTO> authorities
){}
