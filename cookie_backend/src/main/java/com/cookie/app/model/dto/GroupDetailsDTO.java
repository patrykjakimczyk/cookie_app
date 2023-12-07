package com.cookie.app.model.dto;

import java.util.List;

public record GroupDetailsDTO(
    Long id,
    String groupName,
    UserDTO creator,
    List<UserDTO> users
) {}
