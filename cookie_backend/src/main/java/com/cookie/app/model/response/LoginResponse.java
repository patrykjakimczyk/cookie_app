package com.cookie.app.model.response;

import com.cookie.app.model.dto.AuthorityDTO;

import java.util.Set;

public record LoginResponse(String username, boolean assignedPantry) {}
