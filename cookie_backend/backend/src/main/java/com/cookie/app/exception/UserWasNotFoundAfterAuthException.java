package com.cookie.app.exception;

public class UserWasNotFoundAfterAuthException extends  RuntimeException{
    public UserWasNotFoundAfterAuthException(String message) {
        super(message);
    }
}
