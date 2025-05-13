package com.cookie.app.exception;

public class UserPerformedForbiddenActionException extends RuntimeException{
    public UserPerformedForbiddenActionException(String message) {
        super(message);
    }
}
