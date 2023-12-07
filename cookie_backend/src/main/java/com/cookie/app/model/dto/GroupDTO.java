package com.cookie.app.model.dto;

public record GroupDTO(
        Long id,
        String groupName,
        UserDTO creator,
        int users
) {}
