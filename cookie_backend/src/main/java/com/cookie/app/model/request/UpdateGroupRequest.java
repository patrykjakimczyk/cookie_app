package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import jakarta.validation.constraints.Pattern;

public record UpdateGroupRequest(
        @Pattern(
                regexp = RegexConstants.GROUP_NAME_REGEX,
                message = "Pantry name can only contains those symbols (a-z, A-Z, 0-9, '_') and its length has to be between 3 and 20"
        )
        String newGroupName
) {}
