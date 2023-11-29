package com.cookie.app.model.enums;

import java.util.HashSet;
import java.util.Set;

public enum AuthorityEnum {
    READ,
    ADD,
    DELETE,
    RESERVE,
    MODIFY,
    MODIFY_PANTRY,
    DELETE_PANTRY;

    public final static Set<AuthorityEnum> ALL_AUTHORITIES = Set.of(READ, ADD, DELETE, RESERVE, MODIFY, MODIFY_PANTRY, DELETE_PANTRY);
}
