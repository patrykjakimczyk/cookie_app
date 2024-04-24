package com.cookie.app.model;

public class RegexConstants {
    public static final String EMAIL_REGEX = "^([a-zA-Z0-9[.]]{2,})(@)([\\p{L}\\d[-]]{2,})(.)([a-zA-Z]{2,}){0,255}$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]{8,128}$";
    public static final String USERNAME_REGEX = "^([\\p{L}\\d[_]]{6,30})$";
    public static final String PANTRY_NAME_REGEX = "^([\\p{L}\\d\\s]{3,30})$";
    public static final String PLACEMENT_REGEX = "^([\\p{L}\\d\\s]{0,30})$";
    public static final String GROUP_NAME_REGEX = "^([\\p{L}\\d\\s]{3,30})$";
    public static final String PRODUCT_NAME_REGEX = "^([\\p{L}\\d[\\s]-]{3,50})$";
    public static final String SHOPPING_LIST_NAME_REGEX = "^([\\p{L}\\d\\s]{3,30})$";
    public static final String RECIPE_NAME_REGEX = "^([\\p{L}\\d\\s['\":-_@,.]]{5,60})$";
    public static final String CUISINE_REGEX = "^[\\p{L}\\s]+$";
    public static final String PREPARATION_REGEX = "^([\\p{L}\\d\\s]{30,512})$";
    public static final String FILTER_VALUE_REGEX = "^([\\p{L}\\d\\s-])+$";
    public static final String SORT_COL_REGEX = "^[\\p{L}_]+$";
    public static final String SORT_DIRECTION_REGEX = "DESC|ASC";

    private RegexConstants() {}
}
