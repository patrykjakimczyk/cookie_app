package com.cookie.app.model;

public class RegexConstants {
    public final static String EMAIL_REGEX = "^([a-zA-Z0-9[.]]{2,})(@)([a-zA-Z0-9[-]]{2,})(.)([a-zA-Z]{2,}).{0,255}$";
    public final static String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,128}$";
}
