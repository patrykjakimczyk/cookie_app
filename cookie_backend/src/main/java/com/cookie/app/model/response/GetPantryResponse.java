package com.cookie.app.model.response;

import com.cookie.app.model.dto.AuthorityDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record GetPantryResponse(
        @Schema(example = "1")
        long pantryId,
        @Schema(example = "pantryName")
        String pantryName,
        @Schema(example = "1")
        long groupId,
        @Schema(example = "groupName")
        String groupName,
        Set<AuthorityDTO> authorities) {
}
