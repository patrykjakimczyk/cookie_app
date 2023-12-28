package com.cookie.app.model.enums;

import java.util.Set;

public enum AuthorityEnum {
    ADD,
    RESERVE,
    MODIFY,
    MODIFY_PANTRY,
    MODIFY_GROUP,
    ADD_TO_GROUP,
    CREATE_SHOPPING_LIST,
    MODIFY_SHOPPING_LIST,
    ADD_TO_SHOPPING_LIST;

    public static final Set<AuthorityEnum> ALL_AUTHORITIES = Set.of(
            ADD, RESERVE, MODIFY, MODIFY_PANTRY, MODIFY_GROUP, ADD_TO_GROUP, CREATE_SHOPPING_LIST, MODIFY_SHOPPING_LIST, ADD_TO_SHOPPING_LIST
    );
    public static final Set<AuthorityEnum> BASIC_AUTHORITIES = Set.of(ADD, RESERVE, ADD_TO_GROUP, ADD_TO_SHOPPING_LIST);
}
