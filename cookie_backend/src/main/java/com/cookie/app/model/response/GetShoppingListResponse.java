package com.cookie.app.model.response;

import com.cookie.app.model.dto.AuthorityDTO;

import java.util.Set;

public record GetShoppingListResponse(Long id, String listName, Set<AuthorityDTO> authorities) {}
