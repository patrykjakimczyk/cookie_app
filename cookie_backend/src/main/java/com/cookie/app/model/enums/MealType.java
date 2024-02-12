package com.cookie.app.model.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public enum MealType {
    APPETIZER,
    DESSERT,
    DINNER,
    LUNCH,
    SNACK,
    BREAKFAST,
    SOUP;

    public final static Set<MealType> ALL_MEAL_TYPES = Set.of(APPETIZER, DESSERT, DINNER, LUNCH, SNACK, BREAKFAST, SOUP);
}
