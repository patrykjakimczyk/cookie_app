package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdateGroupRequest(
        @Schema(example = "newGroupName")
        @NotNull(message = "Group name must be present")
        @Pattern(
                regexp = RegexConstants.GROUP_NAME_REGEX,
                message = "Pantry name can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 3 and 20"
        )
        String newGroupName
) {}
