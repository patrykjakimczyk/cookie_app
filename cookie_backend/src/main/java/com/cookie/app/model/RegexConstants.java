package com.cookie.app.model;

public class RegexConstants {
    public static final String EMAIL_REGEX = "^([a-zA-Z0-9[.]]{2,})(@)([a-zA-Z0-9[-]]{2,})(.)([a-zA-Z]{2,}){0,255}$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[a-zA-Z\\d@$!%*?&]{8,128}$";
    public static final String USERNAME_REGEX = "^([a-zA-Z\\d[_]]{6,30})$";

    private RegexConstants() {}
}
