package com.cookie.app.model.dto;

import com.cookie.app.model.enums.AuthorityEnum;

public record AuthorityDTO(
        AuthorityEnum authority,
        long userId,
        long groupId
) {}
