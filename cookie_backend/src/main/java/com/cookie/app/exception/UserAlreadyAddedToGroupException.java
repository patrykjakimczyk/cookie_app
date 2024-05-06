package com.cookie.app.exception;

public class UserAlreadyAddedToGroupException extends RuntimeException {
    public UserAlreadyAddedToGroupException(String message) {
        super(message);
    }
}
