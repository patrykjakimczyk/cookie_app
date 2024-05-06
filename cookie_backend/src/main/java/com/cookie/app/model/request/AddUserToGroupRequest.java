package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AddUserToGroupRequest(
        @Schema(example = "username")
        @NotNull(message = "Username must be present")
        @Pattern(regexp = RegexConstants.USERNAME_REGEX, message = "Username is incorrect")
        String usernameToAdd
) {
}
