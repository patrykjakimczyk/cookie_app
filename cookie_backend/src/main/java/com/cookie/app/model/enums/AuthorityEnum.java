package com.cookie.app.model.enums;

import java.util.Set;

public enum AuthorityEnum {
    READ,
    ADD,
    DELETE,
    RESERVE,
    MODIFY,
    MODIFY_PANTRY,
    MODIFY_GROUP,
    ADD_TO_GROUP;

    public static final Set<AuthorityEnum> ALL_AUTHORITIES = Set.of(READ, ADD, DELETE, RESERVE, MODIFY, MODIFY_PANTRY, MODIFY_GROUP, ADD_TO_GROUP);
    public static final Set<AuthorityEnum> BASIC_AUTHORITIES = Set.of(READ, ADD, DELETE, RESERVE, MODIFY);
}
