package com.cookie.app.model.response;

import com.cookie.app.model.dto.AuthorityDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record GetShoppingListResponse(
        @Schema(example = "1")
        Long id,

        @Schema(example = "listname")
        String listName,
        Set<AuthorityDTO> authorities,
        boolean assignedPantry) {}
