package com.cookie.app.model.request;

import com.cookie.app.model.RegexConstants;
import jakarta.validation.constraints.Pattern;

public record AddUserToGroupRequest(
        @Pattern(regexp = RegexConstants.USERNAME_REGEX, message = "Username is incorrect")
        String usernameToAdd
) {
}
