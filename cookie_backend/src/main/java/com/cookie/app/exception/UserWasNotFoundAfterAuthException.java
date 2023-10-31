package com.cookie.app.exception;

public class UserWasNotFoundAfterAuthException extends  RuntimeException{
    public UserWasNotFoundAfterAuthException() {
        super();
    }
    public UserWasNotFoundAfterAuthException(String message, Throwable cause) {
        super(message, cause);
    }
    public UserWasNotFoundAfterAuthException(String message) {
        super(message);
    }
    public UserWasNotFoundAfterAuthException(Throwable cause) {
        super(cause);
    }
}
