package com.cookie.app.exception;

public class UserAlreadyAddedToGroupException extends RuntimeException {
    public UserAlreadyAddedToGroupException() {
        super();
    }
    public UserAlreadyAddedToGroupException(String message, Throwable cause) {
        super(message, cause);
    }
    public UserAlreadyAddedToGroupException(String message) {
        super(message);
    }
    public UserAlreadyAddedToGroupException(Throwable cause) {
        super(cause);
    }
}
