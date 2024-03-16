package com.cookie.app.model.response;

import com.cookie.app.model.dto.AuthorityDTO;

import java.util.Set;

public record GetPantryResponse(long pantryId, String pantryName, long groupId, String groupName, Set<AuthorityDTO> authorities) {
}
