package com.cookie.app.exception;

public class PantryNotFoundException extends RuntimeException {
    public PantryNotFoundException() {
        super();
    }
    public PantryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    public PantryNotFoundException(String message) {
        super(message);
    }
    public PantryNotFoundException(Throwable cause) {
        super(cause);
    }
}
