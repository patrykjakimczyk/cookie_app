package com.cookie.app.util;

public class StringUtil {
    public static boolean isBlank(String string) {
        if(string == null) return true;
        return string.isEmpty();
    }
}
