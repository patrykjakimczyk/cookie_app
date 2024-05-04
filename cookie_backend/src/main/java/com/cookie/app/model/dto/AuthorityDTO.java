package com.cookie.app.model.dto;

import com.cookie.app.model.enums.AuthorityEnum;
import io.swagger.v3.oas.annotations.media.Schema;

public record AuthorityDTO(
        @Schema(example = "ADD")
        AuthorityEnum authority,

        @Schema(example = "1")
        long userId,
        @Schema(example = "1")
        long groupId
) {}
